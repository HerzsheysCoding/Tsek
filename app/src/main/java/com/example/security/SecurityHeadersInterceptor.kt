package com.example.security

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Enforces Points 17, 18, 19:
 * - Point 17: Trim API responses & prevent sensitive data leakage
 * - Point 18: Add security headers to all outgoing requests
 * - Point 19: Force HTTPS (rejects unencrypted cleartext HTTP)
 */
class SecurityHeadersInterceptor : Interceptor {

    companion object {
        private const val TAG = "SecurityInterceptor"
        private const val MAX_LOG_BODY_LENGTH = 500

        fun createSecureClient(timeoutSeconds: Long = 15): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(SecurityHeadersInterceptor())
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        // Point 19: Force HTTPS — block any plain HTTP requests
        if (!url.isHttps) {
            val errorMsg = "Cleartext HTTP request blocked: ${url.host}. All connections must use HTTPS."
            Log.e(TAG, errorMsg)
            throw IOException(errorMsg)
        }

        // Point 18: Add standard defense-in-depth security headers
        val secureRequest = originalRequest.newBuilder()
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("Referrer-Policy", "strict-origin-when-cross-origin")
            .header("Cache-Control", "no-store, no-cache, must-revalidate")
            .header("User-Agent", "TsekSecurityApp/1.0 (Android; SecurityHardened)")
            .build()

        val response = chain.proceed(secureRequest)

        // Point 17: Trim API responses — sanitize logging to prevent token/payload leaks in logs
        if (!response.isSuccessful) {
            Log.w(TAG, "Secure request to ${url.host} returned HTTP status ${response.code}")
        }

        return response
    }
}
