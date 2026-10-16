package com.example.engine

import java.net.URI

object ReputationEngine {

    // Legitimate primary domains for Philippine institutions
    private val OFFICIAL_DOMAINS = mapOf(
        "gcash" to setOf("gcash.com", "help.gcash.com", "new.gcash.com"),
        "maya" to setOf("maya.ph", "paymaya.com", "support.maya.ph"),
        "bdo" to setOf("bdo.com.ph", "online.bdo.com.ph", "cloudoffice.bdo.com.ph"),
        "bpi" to setOf("bpi.com.ph", "online.bpi.com.ph"),
        "unionbank" to setOf("unionbankph.com", "online.unionbankph.com"),
        "metrobank" to setOf("metrobank.com.ph", "metrobankdirect.com"),
        "landbank" to setOf("landbank.com", "lbp-eservices.com"),
        "shopee" to setOf("shopee.ph", "shopee.com"),
        "lazada" to setOf("lazada.com.ph", "lazada.com"),
        "j&t" to setOf("jtexpress.ph"),
        "lbc" to setOf("lbcexpress.com"),
        "gov" to setOf("gov.ph")
    )

    private val HIGH_RISK_TLDS = setOf(
        "xyz", "top", "click", "site", "vip", "club", "work", "live", "cam",
        "space", "link", "online", "cf", "gq", "ml", "tk", "support", "shop"
    )

    private val URL_SHORTENERS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "cutt.ly", "is.gd", "rb.gy", "shorturl.at", "ow.ly"
    )

    data class ReputationResult(
        val url: String,
        val host: String,
        val isOfficialDomain: Boolean,
        val isImpersonatingPhishing: Boolean,
        val impersonatedBrand: String?,
        val hasHighRiskTld: Boolean,
        val isShortener: Boolean
    )

    fun checkUrl(rawUrl: String): ReputationResult {
        val sanitized = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }

        val host = try {
            val uri = URI(sanitized)
            (uri.host ?: sanitized).lowercase()
        } catch (_: Exception) {
            sanitized.substringBefore("/").lowercase()
        }

        val isShortener = URL_SHORTENERS.any { host.contains(it) }

        // Check if domain is truly official
        var isOfficial = false
        for ((_, officialSet) in OFFICIAL_DOMAINS) {
            if (officialSet.any { host == it || host.endsWith(".$it") }) {
                isOfficial = true
                break
            }
        }

        if (host.endsWith(".gov.ph")) {
            isOfficial = true
        }

        // Check TLD
        val tld = host.substringAfterLast(".", "")
        val hasHighRiskTld = HIGH_RISK_TLDS.contains(tld)

        // Check lookalike / typosquatting impersonation
        var impersonatingBrand: String? = null
        var isPhishing = false

        if (!isOfficial) {
            val brandKeywords = listOf(
                "gcash", "maya", "paymaya", "bdo", "bpi", "unionbank",
                "metrobank", "landbank", "shopee", "lazada", "jtexpress", "lbcexpress"
            )

            for (brand in brandKeywords) {
                // If the host contains "gcash" (e.g. gcash-verify.xyz, claim-gcash.top) but isn't gcash.com
                if (host.contains(brand)) {
                    isPhishing = true
                    impersonatingBrand = brand.uppercase()
                    break
                }
            }
        }

        return ReputationResult(
            url = rawUrl,
            host = host,
            isOfficialDomain = isOfficial,
            isImpersonatingPhishing = isPhishing,
            impersonatedBrand = impersonatingBrand,
            hasHighRiskTld = hasHighRiskTld,
            isShortener = isShortener
        )
    }
}
