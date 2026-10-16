package com.example.security

import android.os.SystemClock

/**
 * Enforces Points 11 and 12:
 * - Point 11: Rate limit login (exponential backoff & lockout on repeated failures)
 * - Point 12: Add bot protection (scan rate limiting, anti-automation checks)
 */
object RateLimiter {

    // Login Rate Limiting state
    private var failedLoginAttempts = 0
    private var lockoutUntilElapsedRealtime: Long = 0

    // Scan Rate Limiting state (sliding window: max 15 scans per minute)
    private const val MAX_SCANS_PER_MINUTE = 15
    private val scanTimestamps = mutableListOf<Long>()

    /**
     * Point 11: Checks if login attempt is currently permitted or locked out.
     * Returns Pair(isAllowed, remainingSeconds)
     */
    @Synchronized
    fun checkLoginAllowed(): Pair<Boolean, Long> {
        val now = SystemClock.elapsedRealtime()
        if (now < lockoutUntilElapsedRealtime) {
            val remainingMs = lockoutUntilElapsedRealtime - now
            val remainingSec = (remainingMs / 1000) + 1
            return Pair(false, remainingSec)
        }
        return Pair(true, 0L)
    }

    /**
     * Point 11: Records a failed login attempt and applies progressive lockout.
     */
    @Synchronized
    fun recordLoginFailure(): Long {
        failedLoginAttempts++
        val now = SystemClock.elapsedRealtime()
        val lockoutSeconds = when {
            failedLoginAttempts >= 5 -> 300L // 5 minutes lockout
            failedLoginAttempts >= 3 -> 30L  // 30 seconds cooldown
            else -> 0L
        }

        if (lockoutSeconds > 0) {
            lockoutUntilElapsedRealtime = now + (lockoutSeconds * 1000)
        }
        return lockoutSeconds
    }

    /**
     * Point 11: Resets login attempt counter on successful authentication.
     */
    @Synchronized
    fun recordLoginSuccess() {
        failedLoginAttempts = 0
        lockoutUntilElapsedRealtime = 0
    }

    /**
     * Point 12: Bot Protection - Sliding window rate limiter for scan requests.
     * Prevents automated scripts and bots from flooding the scanning engine.
     */
    @Synchronized
    fun checkScanAllowed(): Pair<Boolean, String> {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60_000

        // Clean up older timestamps
        scanTimestamps.removeAll { it < oneMinuteAgo }

        if (scanTimestamps.size >= MAX_SCANS_PER_MINUTE) {
            return Pair(false, "Rate limit exceeded. Please wait a minute before running more checks.")
        }

        scanTimestamps.add(now)
        return Pair(true, "")
    }

    /**
     * Point 12: Bot Protection - Check minimum human interaction duration.
     * Submissions occurring faster than 300ms from form presentation are flagged as synthetic bot automation.
     */
    fun isHumanTiming(durationMs: Long): Boolean {
        return durationMs >= 300
    }
}
