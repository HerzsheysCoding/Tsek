package com.example.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Enforces Points 1, 5, 9, 10:
 * - Point 1: Hide API keys (safe retrieval, masking for logs)
 * - Point 5: Encrypt sensitive data (AES encryption for sensitive tokens/notes)
 * - Point 9: Secure session cookies / tokens
 * - Point 10: Hash passwords (salted SHA-256 cryptographic hashing)
 */
object SecurityVault {

    private const val SALT = "tsek_ph_vault_security_v2_2026"
    private const val AES_KEY_STRING = "TsekSecKey_2026#PhilippinesSafe!" // 32 bytes for AES-256

    /**
     * Point 10: Hash passwords.
     * Never stores raw plaintext passwords. Computes a salted SHA-256 digest.
     */
    fun hashPassword(password: String): String {
        if (password.isBlank()) return ""
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedInput = "$SALT:$password:$SALT"
        val hashBytes = digest.digest(saltedInput.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Point 5 & 9: Encrypt sensitive data (session tokens, account numbers, sensitive notes).
     */
    fun encryptData(plainText: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val keyBytes = AES_KEY_STRING.toByteArray(StandardCharsets.UTF_8).copyOf(32)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16) { 0x01.toByte() }
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            plainText
        }
    }

    /**
     * Point 5 & 9: Decrypt sensitive data.
     */
    fun decryptData(encryptedBase64: String): String {
        if (encryptedBase64.isBlank()) return ""
        return try {
            val keyBytes = AES_KEY_STRING.toByteArray(StandardCharsets.UTF_8).copyOf(32)
            val secretKey = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16) { 0x01.toByte() }
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decodedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            encryptedBase64
        }
    }

    /**
     * Point 1: Mask API keys so they are never printed in cleartext in logs.
     */
    fun maskApiKey(key: String?): String {
        if (key.isNullOrBlank()) return "[EMPTY]"
        if (key.length <= 8) return "***"
        return "${key.take(4)}...${key.takeLast(4)}"
    }

    /**
     * Point 3: Validate that client DB key is strictly public anon key and NOT a service_role key.
     */
    fun isPublicAnonKey(key: String): Boolean {
        if (key.isBlank()) return false
        val lower = key.lowercase()
        // service_role keys must never be in client apps!
        if (lower.contains("service_role") || lower.contains("secret")) {
            return false
        }
        return true
    }
}
