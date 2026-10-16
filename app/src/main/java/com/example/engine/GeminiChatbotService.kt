package com.example.engine

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.security.SecurityHeadersInterceptor
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

object GeminiChatbotService {

    private const val TAG = "GeminiChatbotService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = SecurityHeadersInterceptor.createSecureClient(timeoutSeconds = 45)

    suspend fun getChatResponse(
        userMessage: String,
        history: List<ChatMessage>,
        isTaglish: Boolean
    ): String = withContext(Dispatchers.IO) {
        val lower = userMessage.lowercase()
        // Deterministic Zero-Latency Emergency Scenario Interceptor
        val isEmergencyOtp = (lower.contains("otp") || lower.contains("mpin")) &&
                (lower.contains("gave") || lower.contains("give") || lower.contains("binigay") ||
                        lower.contains("ibigay") || lower.contains("naibigay") || lower.contains("minute") ||
                        lower.contains("oras") || lower.contains("shared") || lower.contains("sent") ||
                        lower.contains("input") || lower.contains("compromise") || lower.contains("naitype"))

        if (isEmergencyOtp) {
            return@withContext if (isTaglish) {
                """🚨 AGARANG EMERGENCY ACTION CHECKLIST:

Huwag mag-panic. Mahalaga ang unang 1 oras bago ma-withdraw ng scammer ang pera. Gawin ito NGAYON DIN:

1. I-LOCK ANG IYONG ACCOUNT O BAGUHIN ANG MPIN
   Buksan agad ang GCash, Maya, o banking app. Agad na magpalit ng MPIN/password o i-freeze ang debit/credit card.

2. TAWAGAN AGAD ANG E-WALLET O BANGKO:
   • GCash: Tumawag sa 2882
   • Maya: Tumawag sa *788 o (02) 8845-7788
   • BDO: (02) 8631-8000 | BPI: (02) 889-10000
   Hilingin agad na i-freeze ang outgoing transactions at i-hold ang receiving wallet/account.

3. TUMAWAG SA CICC SCAM HOTLINE 1326:
   • 24/7 National Anti-Scam Hotline ng gobyerno para sa agarang koordinasyon.

4. KUNAN NG SCREENSHOT ANG LAHAT NG EBIDENSYA:
   I-save ang reference number, numero ng scammer, oras, at usapan bago ito mabura."""
            } else {
                """🚨 IMMEDIATE EMERGENCY ACTION CHECKLIST:

Do not panic. The first hour is the critical window before scammers can cash out funds. Execute these steps RIGHT NOW:

1. LOCK YOUR ACCOUNT OR CHANGE MPIN IMMEDIATELY
   Open GCash, Maya, or your banking app. Immediately change your MPIN / password or freeze your card.

2. CALL YOUR E-WALLET / BANK FRAUD HOTLINE IMMEDIATELY:
   • GCash: Dial 2882
   • Maya: Dial *788 or (02) 8845-7788
   • BDO: Dial (02) 8631-8000 | BPI: Dial (02) 889-10000
   Tell the fraud agent your OTP was compromised and request an immediate hold on outgoing transfers and recipient accounts.

3. CALL CICC SCAM HOTLINE 1326:
   • 24/7 Government Toll-Free Hotline to coordinate freezing scam funds across Philippine financial institutions.

4. SCREENSHOT AND PRESERVE EVIDENCE:
   Save reference numbers, suspect phone numbers, exact timestamps, and chat transcripts before they are deleted."""
            }
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext if (isTaglish) {
                "Paalala: HINDING-HINDI hihingin ng GCash, Maya, o bangko ang iyong OTP o MPIN. Kapag may humingi nito, 100% scam ito! Huwag i-click ang link at i-block ang numero."
            } else {
                "Golden Security Rule: Legitimate Philippine banks and e-wallets NEVER ask for your OTP or MPIN. If anyone requests it, it is 100% a scam. Never click suspicious links and block the sender."
            }
        }

        try {
            val systemInstruction = """
                You are Tsek AI, an authoritative, friendly, and vigilant cybersecurity anti-fraud expert for Filipinos.
                You help users identify scam messages, fake GCash/Maya SMS, fake parcel deliveries, illegal job task scams, and phishing links.
                Guidelines:
                1. Always prioritize user safety: emphasize NEVER sharing OTP, MPIN, or passwords.
                2. If the user shares a message or link, evaluate the red flags clearly.
                3. Keep answers concise, clear, and easy to read (bullet points where helpful).
                4. Tone is calm, supportive, and non-judgmental.
                5. Answer in ${if (isTaglish) "simple Taglish" else "crisp English"}.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Include prior turns for conversational context (up to last 6 messages)
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.isUser) "user" else "model"
                val turnObj = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            }
            contentsArray.put(currentTurn)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
                put(
                    "generationConfig",
                    JSONObject().apply {
                        put("temperature", 0.4)
                        put("maxOutputTokens", 800)
                    }
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
                Log.w(TAG, "Gemini Chat API returned ${response.code}: ${response.message}")
                return@withContext if (isTaglish) {
                    "Paumanhin, pansamantalang nagka-aberya sa network. Tandaan: Huwag ibigay ang OTP o mag-click sa kakaibang link."
                } else {
                    "Temporary network notice: Always remember never to disclose OTPs or click external links."
                }
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val replyText = jsonResponse
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            replyText?.trim()?.ifBlank { null } ?: if (isTaglish) {
                "Suriin nang mabuti ang nagpadala. Laging buksan ang opisyal na app."
            } else {
                "Please check the sender carefully and access accounts only through verified mobile apps."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini chatbot call", e)
            if (isTaglish) {
                "Paalala: Kapag may humihingi ng pera o OTP, huwag munang magpadala at magtanong sa pinagkakatiwalaang kapamilya."
            } else {
                "Security tip: If anyone pressures you for money or OTP verification, verify directly via official hotlines."
            }
        }
    }
}
