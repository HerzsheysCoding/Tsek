package com.example.engine

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ExtractedEntities
import com.example.data.model.RedFlag
import com.example.data.model.RiskVerdict
import com.example.data.model.ScamCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.security.SecurityHeadersInterceptor
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeminiAnalysisResult(
    val verdict: RiskVerdict,
    val score: Int,
    val category: ScamCategory,
    val explanationTl: String,
    val explanationEn: String,
    val redFlags: List<RedFlag>,
    val adviceListTl: List<String>,
    val adviceListEn: List<String>
)

object GeminiScamAnalyzer {

    private const val TAG = "GeminiScamAnalyzer"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = SecurityHeadersInterceptor.createSecureClient(timeoutSeconds = 45)

    suspend fun analyze(
        text: String,
        bitmap: Bitmap? = null,
        entities: ExtractedEntities,
        ruleScore: Int
    ): GeminiAnalysisResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key is not configured; using local rule engine.")
            return@withContext null
        }

        try {
            val systemInstruction = """
                You are Tsek, a Philippine fraud detection and cybersecurity specialist.
                Evaluate the provided text or screenshot for scams, phishing, or financial fraud targeting Filipinos.
                Philippine context includes:
                - GCash, Maya, BDO, BPI, UnionBank, Shopee, Lazada, J&T, LBC.
                - OTP/MPIN theft is ALWAYS HIGH_RISK.
                - Never output 'Safe'; if no red flags found, state 'NO_RED_FLAGS'.
                - Explain in simple, friendly Taglish (Grade 6 level) that a Filipino parent, student, or senior can easily understand.
                
                You MUST reply ONLY with a valid JSON object with the following schema:
                {
                  "risk_level": "HIGH_RISK" | "SUSPICIOUS" | "NO_RED_FLAGS",
                  "score": number (0 to 100),
                  "scam_category": "FAKE_EWALLET" | "OTP_THEFT" | "FAKE_JOB_TASK" | "PARCEL_DELIVERY" | "PHISHING_LINK" | "FAKE_LOAN" | "LOTTERY_PRIZE" | "UNKNOWN",
                  "explanation_tl": "Short plain-language Taglish explanation of why it is dangerous or what was found",
                  "explanation_en": "Short plain English explanation",
                  "red_flags_tl": ["flag 1 in Taglish", "flag 2 in Taglish"],
                  "red_flags_en": ["flag 1 in English", "flag 2 in English"],
                  "advice_tl": ["specific action in Taglish", "action 2"],
                  "advice_en": ["specific action in English", "action 2"]
                }
            """.trimIndent()

            val partsArray = JSONArray()

            val promptText = buildString {
                append("Suspicious Content to Analyze:\n")
                append(text.ifBlank { "[Image/Screenshot uploaded]" })
                append("\n\nExtracted Entities: ")
                append("URLs: ${entities.urls.joinToString()}; ")
                append("Phone numbers: ${entities.phPhoneNumbers.joinToString()}; ")
                append("Brands: ${entities.brands.joinToString()}; ")
                append("Asks OTP/PIN: ${entities.asksOtpOrPin}; ")
                append("Local Rule Score: $ruleScore/100")
            }

            partsArray.put(JSONObject().put("text", promptText))

            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                partsArray.put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject()
                            .put("mimeType", "image/jpeg")
                            .put("data", base64Image)
                    )
                )
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
                put(
                    "generationConfig",
                    JSONObject()
                        .put("responseMimeType", "application/json")
                        .put("temperature", 0.2)
                )
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val modelName = "gemini-3.5-flash"
            val url = "$BASE_URL/$modelName:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API call failed with HTTP ${response.code}: ${response.message}")
                return@withContext null
            }

            val responseBody = response.body?.string() ?: return@withContext null
            val responseObj = JSONObject(responseBody)
            val candidateText = responseObj
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")
                ?: return@withContext null

            val parsedJson = JSONObject(candidateText)
            val riskStr = parsedJson.optString("risk_level", "SUSPICIOUS")
            val scoreVal = parsedJson.optInt("score", ruleScore).coerceIn(0, 100)
            val categoryStr = parsedJson.optString("scam_category", "UNKNOWN")
            val explTl = parsedJson.optString("explanation_tl", "")
            val explEn = parsedJson.optString("explanation_en", "")

            val redFlagsTlArray = parsedJson.optJSONArray("red_flags_tl") ?: JSONArray()
            val redFlagsEnArray = parsedJson.optJSONArray("red_flags_en") ?: JSONArray()
            val flagsList = mutableListOf<RedFlag>()
            for (i in 0 until maxOf(redFlagsTlArray.length(), redFlagsEnArray.length())) {
                val fTl = redFlagsTlArray.optString(i, "Red flag detected")
                val fEn = redFlagsEnArray.optString(i, "Red flag detected")
                flagsList.add(
                    RedFlag(
                        titleTl = fTl,
                        titleEn = fEn,
                        descriptionTl = fTl,
                        descriptionEn = fEn,
                        isCritical = scoreVal >= 75
                    )
                )
            }

            val adviceTlArray = parsedJson.optJSONArray("advice_tl") ?: JSONArray()
            val adviceEnArray = parsedJson.optJSONArray("advice_en") ?: JSONArray()
            val adviceListTl = mutableListOf<String>()
            val adviceListEn = mutableListOf<String>()

            for (i in 0 until adviceTlArray.length()) {
                adviceListTl.add(adviceTlArray.optString(i))
            }
            for (i in 0 until adviceEnArray.length()) {
                adviceListEn.add(adviceEnArray.optString(i))
            }

            val verdict = when (riskStr.uppercase()) {
                "HIGH_RISK" -> RiskVerdict.HIGH_RISK
                "SUSPICIOUS" -> RiskVerdict.SUSPICIOUS
                else -> RiskVerdict.NO_RED_FLAGS
            }

            val category = try {
                ScamCategory.valueOf(categoryStr.uppercase())
            } catch (_: Exception) {
                ScamCategory.UNKNOWN
            }

            GeminiAnalysisResult(
                verdict = verdict,
                score = scoreVal,
                category = category,
                explanationTl = explTl,
                explanationEn = explEn,
                redFlags = flagsList,
                adviceListTl = adviceListTl,
                adviceListEn = adviceListEn
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error performing Gemini scam analysis", e)
            null
        }
    }
}
