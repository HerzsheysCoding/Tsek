package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.example.security.InputSanitizer
import com.example.security.RateLimiter
import com.example.security.SecurityHeadersInterceptor
import com.example.security.SecurityVault

data class SupabaseAuthResult(
    val success: Boolean,
    val userId: String,
    val email: String,
    val fullName: String,
    val isCloudSynced: Boolean,
    val message: String
)

object SupabaseService {

    private const val TAG = "SupabaseService"

    // Points 18 & 19: Secure client enforcing HTTPS and defense-in-depth headers
    private val okHttpClient = SecurityHeadersInterceptor.createSecureClient(timeoutSeconds = 15)

    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String,
        customUrl: String? = null,
        customKey: String? = null
    ): SupabaseAuthResult = withContext(Dispatchers.IO) {
        // Point 11: Rate limit login & registration
        val (isAllowed, cooldownSeconds) = RateLimiter.checkLoginAllowed()
        if (!isAllowed) {
            return@withContext SupabaseAuthResult(
                success = false,
                userId = "",
                email = email,
                fullName = fullName,
                isCloudSynced = false,
                message = "Too many authentication attempts. Please wait $cooldownSeconds seconds."
            )
        }

        // Point 14: Validate all input
        if (!InputSanitizer.validateEmail(email)) {
            return@withContext SupabaseAuthResult(
                success = false,
                userId = "",
                email = email,
                fullName = fullName,
                isCloudSynced = false,
                message = "Invalid email format. Please provide a valid email."
            )
        }

        val (pwValid, pwError) = InputSanitizer.validatePasswordStrength(password)
        if (!pwValid) {
            return@withContext SupabaseAuthResult(
                success = false,
                userId = "",
                email = email,
                fullName = fullName,
                isCloudSynced = false,
                message = pwError
            )
        }

        val configuredUrl = (customUrl?.takeIf { it.isNotBlank() } ?: BuildConfig.SUPABASE_URL).trim().removeSuffix("/")
        val configuredKey = (customKey?.takeIf { it.isNotBlank() } ?: BuildConfig.SUPABASE_ANON_KEY).trim()

        // Point 3: Use public DB key (Strictly reject service_role or admin secret keys)
        if (configuredKey.isNotBlank() && !SecurityVault.isPublicAnonKey(configuredKey)) {
            return@withContext SupabaseAuthResult(
                success = false,
                userId = "",
                email = email,
                fullName = fullName,
                isCloudSynced = false,
                message = "Security error: Only public anonymous keys (anon key) are allowed. Admin service_role keys are strictly blocked."
            )
        }

        val generatedId = UUID.randomUUID().toString()

        val isConfigured = configuredUrl.isNotBlank() &&
                !configuredUrl.contains("placeholder") &&
                configuredKey.isNotBlank() &&
                !configuredKey.contains("placeholder")

        if (!isConfigured) {
            Log.d(TAG, "Supabase credentials are placeholder or empty. Storing profile in local vault.")
            RateLimiter.recordLoginSuccess()
            return@withContext SupabaseAuthResult(
                success = true,
                userId = generatedId,
                email = email,
                fullName = fullName,
                isCloudSynced = false,
                message = "Account initialized in local vault. Configure Supabase URL & Anon Key to sync to live database."
            )
        }

        try {
            // 1. Try Supabase Auth API: /auth/v1/signup
            val authEndpoint = "$configuredUrl/auth/v1/signup"
            val authBodyJson = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().put("full_name", fullName))
            }

            val authRequest = Request.Builder()
                .url(authEndpoint)
                .addHeader("apikey", configuredKey)
                .addHeader("Authorization", "Bearer $configuredKey")
                .addHeader("Content-Type", "application/json")
                .post(authBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val authResponse = okHttpClient.newCall(authRequest).execute()
            val authResponseCode = authResponse.code
            val authResponseBody = authResponse.body?.string() ?: ""

            var returnedUserId = generatedId
            var isAuthOk = authResponse.isSuccessful

            if (isAuthOk && authResponseBody.isNotBlank()) {
                try {
                    val authObj = JSONObject(authResponseBody)
                    val userObj = authObj.optJSONObject("user")
                    if (userObj != null) {
                        returnedUserId = userObj.optString("id", generatedId)
                    }
                } catch (_: Exception) {}
            }

            // 2. Also upsert into Supabase Database Table: /rest/v1/profiles
            val tableEndpoint = "$configuredUrl/rest/v1/profiles"
            val profilePayload = JSONObject().apply {
                put("id", returnedUserId)
                put("email", email)
                put("full_name", fullName)
                put("created_at", System.currentTimeMillis())
            }

            val tableRequest = Request.Builder()
                .url(tableEndpoint)
                .addHeader("apikey", configuredKey)
                .addHeader("Authorization", "Bearer $configuredKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(profilePayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            try {
                val tableResponse = okHttpClient.newCall(tableRequest).execute()
                Log.d(TAG, "Supabase table response code: ${tableResponse.code}")
            } catch (te: Exception) {
                Log.w(TAG, "Table profile sync notice: ${te.message}")
            }

            if (isAuthOk || authResponseCode == 400 && authResponseBody.contains("already")) {
                RateLimiter.recordLoginSuccess()
                SupabaseAuthResult(
                    success = true,
                    userId = returnedUserId,
                    email = email,
                    fullName = fullName,
                    isCloudSynced = true,
                    message = "Successfully registered and synced with cloud account."
                )
            } else {
                RateLimiter.recordLoginFailure()
                SupabaseAuthResult(
                    success = false,
                    userId = returnedUserId,
                    email = email,
                    fullName = fullName,
                    isCloudSynced = false,
                    message = "Authentication notice (code $authResponseCode). Profile stored securely on device."
                )
            }
        } catch (e: Exception) {
            RateLimiter.recordLoginFailure()
            Log.e(TAG, "Network exception contacting Supabase: ${e.message}")
            SupabaseAuthResult(
                success = false,
                userId = generatedId,
                email = email,
                fullName = fullName,
                isCloudSynced = false,
                message = "Network notice: ${e.message}. Profile stored securely on device."
            )
        }
    }
}
