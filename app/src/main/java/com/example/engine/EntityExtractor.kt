package com.example.engine

import com.example.data.model.ExtractedEntities
import java.util.regex.Pattern

object EntityExtractor {

    private val URL_PATTERN = Pattern.compile(
        """(https?://[^\s/$.?#].[^\s]*)|([a-zA-Z0-9-]+\.(?:com|ph|net|org|xyz|top|site|club|click|link|online|live|vip|work|info|tk|ml|ga|cf|gq|shop)[^\s]*)""",
        Pattern.CASE_INSENSITIVE
    )

    // Philippine Mobile Numbers: 09XXXXXXXXX or +639XXXXXXXXX with optional spaces or dashes
    private val PH_PHONE_PATTERN = Pattern.compile(
        """(\+?63\s?9\d{2}[\s-]?\d{3}[\s-]?\d{4})|(09\d{2}[\s-]?\d{3}[\s-]?\d{4})""",
        Pattern.CASE_INSENSITIVE
    )

    private val BRAND_LIST = listOf(
        "GCash", "Maya", "PayMaya", "BDO", "BPI", "UnionBank", "Metrobank",
        "Landbank", "Shopee", "Lazada", "TikTok Shop", "TikTok", "J&T Express", "J&T",
        "LBC", "Flash Express", "PhilPost", "SSS", "GSIS", "Pag-IBIG", "DFA", "BIR"
    )

    private val URGENCY_KEYWORDS = listOf(
        "24 hours", "12 hours", "within 24", "loob ng 24", "loob ng 12", "oras lamang",
        "immediately", "agad", "urgent", "blocked", "restricted", "deactivated",
        "deactivation", "masususpinde", "suspend", "temporary lock", "unauthorized login",
        "action required", "kailangan agad", "bago mawala"
    )

    private val SENSITIVE_KEYWORDS = listOf(
        "otp", "mpin", "pin", "cvv", "one-time pin", "one time password", "password",
        "security code", "passcode"
    )

    fun extract(text: String): ExtractedEntities {
        val urls = mutableListOf<String>()
        val urlMatcher = URL_PATTERN.matcher(text)
        while (urlMatcher.find()) {
            val url = urlMatcher.group()
            if (!urls.contains(url)) {
                urls.add(url)
            }
        }

        val phones = mutableListOf<String>()
        val phoneMatcher = PH_PHONE_PATTERN.matcher(text)
        while (phoneMatcher.find()) {
            val phone = phoneMatcher.group()
            if (!phones.contains(phone)) {
                phones.add(phone)
            }
        }

        val lowerText = text.lowercase()
        val brands = BRAND_LIST.filter { brand ->
            lowerText.contains(brand.lowercase())
        }

        val urgencyMatches = URGENCY_KEYWORDS.filter { keyword ->
            lowerText.contains(keyword)
        }

        val asksOtp = SENSITIVE_KEYWORDS.any { keyword ->
            // Match whole word for short keywords like otp, pin
            val pattern = Pattern.compile("""\b${Pattern.quote(keyword)}\b""", Pattern.CASE_INSENSITIVE)
            pattern.matcher(text).find()
        }

        return ExtractedEntities(
            urls = urls,
            phPhoneNumbers = phones,
            brands = brands,
            urgencyPhrases = urgencyMatches,
            asksOtpOrPin = asksOtp
        )
    }

    /**
     * Privacy-First Sanitization:
     * Masks real personal credit card numbers, bank account numbers, 6-digit OTPs, and sensitive balance figures
     * before any text is sent over external network services.
     */
    fun sanitizeSensitiveData(text: String): String {
        var sanitized = text
        // 1. Mask 4-6 digit standalone OTP/PIN codes (e.g., "Your OTP is 123456" -> "Your OTP is [REDACTED_OTP]")
        sanitized = sanitized.replace(Regex("""(?i)\b(otp|pin|code|mpin)[\s:=]+(\d{4,6})\b""")) { mr ->
            "${mr.groupValues[1]}: [REDACTED_OTP]"
        }
        // 2. Mask 3-4 digit CVV/CVC security codes
        sanitized = sanitized.replace(Regex("""(?i)\b(cvv|cvc|security code)[\s:=]+(\d{3,4})\b""")) { mr ->
            "${mr.groupValues[1]}: [REDACTED_CVV]"
        }
        // 3. Mask passwords
        sanitized = sanitized.replace(Regex("""(?i)\b(password|passphrase|pwd)[\s:=]+(\S+)\b""")) { mr ->
            "${mr.groupValues[1]}: [REDACTED_PASSWORD]"
        }
        // 4. Mask 16-digit card numbers or 10-12 digit bank account numbers
        sanitized = sanitized.replace(Regex("""\b(\d{4}[-\s]?\d{4}[-\s]?\d{4}[-\s]?\d{4})\b"""), "[REDACTED_CARD]")
        sanitized = sanitized.replace(Regex("""\b(\d{10,12})\b"""), "[REDACTED_ACCOUNT]")
        // 5. Mask personal peso balance mentions (e.g. "Balance: PHP 45,210.00" -> "Balance: [REDACTED_BALANCE]")
        sanitized = sanitized.replace(Regex("""(?i)(?:balance|available|savings)[\s:=]+(?:php|p|₱)?\s?([\d,]+\.\d{2})"""), "Balance: [REDACTED_BALANCE]")
        return sanitized
    }
}
