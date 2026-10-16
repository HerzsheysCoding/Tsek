package com.example.security

import android.util.Patterns
import java.net.URI

/**
 * Enforces Points 14 and 15:
 * - Point 14: Validate all input
 * - Point 15: Escape user content (prevent XSS, injection, control-byte corruption)
 */
object InputSanitizer {

    private const val MAX_INPUT_LENGTH = 50_000

    /**
     * Point 15: Escape user content to neutralize HTML, script, or markup injections.
     */
    fun escapeHtml(input: String): String {
        return buildString(input.length) {
            for (char in input) {
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#x27;")
                    '\u0000' -> {} // Strip null byte
                    else -> {
                        // Strip non-printable ASCII control characters except newline and tab
                        if (char.code in 0..31 && char != '\n' && char != '\r' && char != '\t') {
                            // skip control char
                        } else {
                            append(char)
                        }
                    }
                }
            }
        }
    }

    /**
     * Point 14: Validate scam check input text.
     */
    fun validateCheckInput(text: String): Pair<Boolean, String> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Pair(false, "Input cannot be empty.")
        }
        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Pair(false, "Input exceeds maximum limit of $MAX_INPUT_LENGTH characters.")
        }
        return Pair(true, "")
    }

    /**
     * Point 14: Validate email address.
     */
    fun validateEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
    }

    /**
     * Point 14: Validate password strength.
     * Minimum 8 characters, at least 1 digit, at least 1 letter.
     */
    fun validatePasswordStrength(password: String): Pair<Boolean, String> {
        if (password.length < 8) {
            return Pair(false, "Password must be at least 8 characters long.")
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return Pair(false, "Password must contain both letters and numbers.")
        }
        return Pair(true, "")
    }

    /**
     * Point 14: Validate web URLs.
     */
    fun isValidUrl(urlStr: String): Boolean {
        return try {
            val uri = URI(urlStr.trim())
            val scheme = uri.scheme?.lowercase()
            (scheme == "https" || scheme == "http") && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Point 14: Validate Philippine mobile phone numbers.
     */
    fun isValidPhPhoneNumber(phone: String): Boolean {
        val digitsOnly = phone.replace("[^0-9+]".toRegex(), "")
        return digitsOnly.matches("^(09|\\+639)\\d{9}$".toRegex())
    }
}
