package com.example.data.model

enum class RiskVerdict {
    HIGH_RISK,
    SUSPICIOUS,
    NO_RED_FLAGS;

    fun getDisplayName(isTaglish: Boolean): String {
        return when (this) {
            HIGH_RISK -> if (isTaglish) "Mataas ang Panganib (Scam)" else "High Risk (Likely Scam)"
            SUSPICIOUS -> if (isTaglish) "Kahina-hinala (Magduda)" else "Suspicious (High Caution)"
            NO_RED_FLAGS -> if (isTaglish) "Walang Nakitang Red Flag" else "No Red Flags Found"
        }
    }
}

enum class ScamCategory {
    FAKE_EWALLET,
    OTP_THEFT,
    FAKE_JOB_TASK,
    PARCEL_DELIVERY,
    PHISHING_LINK,
    FAKE_LOAN,
    LOTTERY_PRIZE,
    UNKNOWN;

    fun getDisplayName(isTaglish: Boolean): String {
        return when (this) {
            FAKE_EWALLET -> if (isTaglish) "Pekeng GCash / Maya" else "Fake GCash / Maya"
            OTP_THEFT -> if (isTaglish) "Pagnanakaw ng OTP / PIN" else "OTP / MPIN Theft"
            FAKE_JOB_TASK -> if (isTaglish) "Fake Part-Time Job / Task" else "Job Task Scam"
            PARCEL_DELIVERY -> if (isTaglish) "Pekeng Delivery / Parcel COD" else "Fake Parcel Delivery"
            PHISHING_LINK -> if (isTaglish) "Phishing Link / Pekeng Website" else "Phishing Link"
            FAKE_LOAN -> if (isTaglish) "Mapanlinlang na Online Loan" else "Predatory Loan App"
            LOTTERY_PRIZE -> if (isTaglish) "Pekeng Ayuda o Premyo" else "Fake Prize / Subsidy"
            UNKNOWN -> if (isTaglish) "Ibang Uri ng Panloloko" else "Suspicious Activity"
        }
    }
}

data class RedFlag(
    val titleTl: String,
    val titleEn: String,
    val descriptionTl: String,
    val descriptionEn: String,
    val isCritical: Boolean = false
)

data class ExtractedEntities(
    val urls: List<String> = emptyList(),
    val phPhoneNumbers: List<String> = emptyList(),
    val brands: List<String> = emptyList(),
    val urgencyPhrases: List<String> = emptyList(),
    val asksOtpOrPin: Boolean = false
)

data class CheckResult(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawInput: String,
    val inputType: String = "TEXT", // "TEXT", "LINK", "PHONE", "SCREENSHOT"
    val verdict: RiskVerdict,
    val score: Int, // 0 - 100
    val category: ScamCategory,
    val explanationTl: String,
    val explanationEn: String,
    val adviceListTl: List<String>,
    val adviceListEn: List<String>,
    val redFlags: List<RedFlag>,
    val entities: ExtractedEntities,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrendingAlert(
    val id: String,
    val titleTl: String,
    val titleEn: String,
    val category: ScamCategory,
    val severity: RiskVerdict,
    val targetBrand: String,
    val sampleMessage: String,
    val redFlagsTl: List<String>,
    val redFlagsEn: List<String>,
    val howToProtectTl: String,
    val howToProtectEn: String,
    val source: String = "CICC & PNP-ACG Advisory",
    val publishedDate: String = "2 days ago",
    val reportCount: Int = 142
)

enum class AppLanguage {
    TAGLISH,
    ENGLISH
}
