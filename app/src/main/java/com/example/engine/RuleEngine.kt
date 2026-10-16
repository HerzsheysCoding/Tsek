package com.example.engine

import com.example.data.model.ExtractedEntities
import com.example.data.model.RedFlag
import com.example.data.model.RiskVerdict
import com.example.data.model.ScamCategory

data class RuleEvaluation(
    val verdict: RiskVerdict,
    val score: Int,
    val category: ScamCategory,
    val redFlags: List<RedFlag>,
    val explanationTl: String,
    val explanationEn: String,
    val adviceListTl: List<String>,
    val adviceListEn: List<String>
)

object RuleEngine {

    fun evaluate(text: String, entities: ExtractedEntities, reputationResults: List<ReputationEngine.ReputationResult>): RuleEvaluation {
        val redFlags = mutableListOf<RedFlag>()
        val lowerText = text.lowercase()
        var score = 15 // Baseline skepticism

        // Rule 1: Asking for OTP / MPIN / PIN (Instant Critical Red Flag)
        if (entities.asksOtpOrPin) {
            score += 65
            redFlags.add(
                RedFlag(
                    titleTl = "Nanghihingi ng OTP o MPIN",
                    titleEn = "Requests OTP / MPIN",
                    descriptionTl = "HINDI kailanman hihingin ng GCash, Maya, o bangko ang iyong One-Time PIN. Ito ay direktang pagnanakaw ng pera.",
                    descriptionEn = "Legitimate institutions NEVER request your OTP or PIN. This is a direct attempt to drain your account.",
                    isCritical = true
                )
            )
        }

        // Rule 2: Phishing Lookalike Domain Impersonation
        val phishingDomain = reputationResults.firstOrNull { it.isImpersonatingPhishing }
        if (phishingDomain != null) {
            score += 60
            redFlags.add(
                RedFlag(
                    titleTl = "Pekeng Website Link (${phishingDomain.host})",
                    titleEn = "Impersonated Phishing Link (${phishingDomain.host})",
                    descriptionTl = "Ginagaya ng website na ito ang ${phishingDomain.impersonatedBrand ?: "opisyal na kumpanya"} upang nakawin ang iyong login credentials.",
                    descriptionEn = "This fake link impersonates ${phishingDomain.impersonatedBrand ?: "the brand"} to steal credentials.",
                    isCritical = true
                )
            )
        }

        // Rule 3: High Risk TLDs (.xyz, .top, .click)
        val riskyTldDomain = reputationResults.firstOrNull { it.hasHighRiskTld }
        if (riskyTldDomain != null) {
            score += 30
            redFlags.add(
                RedFlag(
                    titleTl = "Kwestiyonableng Domain Extension",
                    titleEn = "High-Risk Domain Extension",
                    descriptionTl = "Gumagamit ang link ng kakaibang TLD tulad ng .${riskyTldDomain.host.substringAfterLast(".")} na madalas gamitin ng scammers.",
                    descriptionEn = "The link uses a suspicious domain extension (.${riskyTldDomain.host.substringAfterLast(".")}) favored by fraudsters.",
                    isCritical = false
                )
            )
        }

        // Rule 4: High Urgency / Threat Pressure
        if (entities.urgencyPhrases.isNotEmpty()) {
            score += 20
            redFlags.add(
                RedFlag(
                    titleTl = "Sobra ang Pagmamadali ('${entities.urgencyPhrases.first()}')",
                    titleEn = "Artificial Urgency & Pressure",
                    descriptionTl = "Pinagmamadali ka upang matakot at mag-click nang hindi nag-iisip.",
                    descriptionEn = "Psychological pressure forcing victims to act before verifying.",
                    isCritical = false
                )
            )
        }

        // Rule 5: Task / Part-time Job Pattern
        val isJobTaskScam = (lowerText.contains("part-time") || lowerText.contains("part time") || lowerText.contains("home-based") || lowerText.contains("liking products") || lowerText.contains("product review")) &&
                (lowerText.contains("telegram") || lowerText.contains("whatsapp") || lowerText.contains("daily") || lowerText.contains("sweldo") || lowerText.contains("earn"))
        if (isJobTaskScam) {
            score += 45
            redFlags.add(
                RedFlag(
                    titleTl = "Pekeng Job Offer / Task Scam",
                    titleEn = "Fake Online Task / Job Scam",
                    descriptionTl = "Nangangako ng malaking kita (PHP 1,000-5,000/araw) sa simpleng pag-like. Sa huli hihingan ka ng pera o 'deposit'.",
                    descriptionEn = "Promises high daily returns for simple tasks, but later requires deposits.",
                    isCritical = false
                )
            )
        }

        // Rule 6: Fake Parcel / Delivery Fee
        val isParcelScam = (lowerText.contains("package") || lowerText.contains("parcel") || lowerText.contains("delivery") || lowerText.contains("j&t") || lowerText.contains("lbc")) &&
                (lowerText.contains("fee") || lowerText.contains("bayad") || lowerText.contains("redelivery") || lowerText.contains("address") || lowerText.contains("unclaimed"))
        if (isParcelScam && entities.urls.isNotEmpty()) {
            score += 40
            redFlags.add(
                RedFlag(
                    titleTl = "Pekeng Parcel Redelivery Fee",
                    titleEn = "Fake Courier / Delivery Scam",
                    descriptionTl = "Nanghihingi ng karagdagang bayad sa website para sa kunwaring delivery.",
                    descriptionEn = "Demands unexpected fees or personal details via external SMS links.",
                    isCritical = false
                )
            )
        }

        // Rule 7: Free Money / Lottery / Ayuda
        val isPrizeScam = (lowerText.contains("ayuda") || lowerText.contains("nanalo") || lowerText.contains("winner") || lowerText.contains("raffle") || lowerText.contains("free 5") || lowerText.contains("subsidy")) &&
                (entities.urls.isNotEmpty() || entities.phPhoneNumbers.isNotEmpty())
        if (isPrizeScam) {
            score += 45
            redFlags.add(
                RedFlag(
                    titleTl = "Pekeng Premyo o Ayuda Trap",
                    titleEn = "Unsolicited Prize / Subsidy Bait",
                    descriptionTl = "Ginagamit ang libreng pera o ayuda bilang pain para makuha ang iyong personal details.",
                    descriptionEn = "Exploits monetary giveaways to lure victims into providing sensitive info.",
                    isCritical = false
                )
            )
        }

        // Deductions if clean:
        val allDomainsOfficial = reputationResults.isNotEmpty() && reputationResults.all { it.isOfficialDomain }
        if (allDomainsOfficial && !entities.asksOtpOrPin && entities.urgencyPhrases.isEmpty()) {
            score = 10
        }

        val finalScore = score.coerceIn(0, 100)

        val verdict = when {
            finalScore >= 65 -> RiskVerdict.HIGH_RISK
            finalScore >= 35 -> RiskVerdict.SUSPICIOUS
            else -> RiskVerdict.NO_RED_FLAGS
        }

        val category = when {
            entities.asksOtpOrPin -> ScamCategory.OTP_THEFT
            phishingDomain != null -> ScamCategory.PHISHING_LINK
            isJobTaskScam -> ScamCategory.FAKE_JOB_TASK
            isParcelScam -> ScamCategory.PARCEL_DELIVERY
            isPrizeScam -> ScamCategory.LOTTERY_PRIZE
            entities.brands.any { it.equals("GCash", true) || it.equals("Maya", true) } -> ScamCategory.FAKE_EWALLET
            else -> ScamCategory.UNKNOWN
        }

        val (explanationTl, explanationEn) = when (verdict) {
            RiskVerdict.HIGH_RISK -> {
                val reasons = redFlags.joinToString(", ") { it.titleTl }
                Pair(
                    "Malamang scam ito. May mga red flag tulad ng: $reasons.",
                    "Likely a scam. Found critical red flags including: ${redFlags.joinToString(", ") { it.titleEn }}."
                )
            }
            RiskVerdict.SUSPICIOUS -> {
                Pair(
                    "Kahina-hinala ito. May mga senyales ng pagmamadali o hindi beripikadong pinagmulan.",
                    "This message appears suspicious. It contains risk indicators such as pressure tactics."
                )
            }
            RiskVerdict.NO_RED_FLAGS -> {
                Pair(
                    "Walang nakitang red flag. Hindi ito garantiya.",
                    "No red flags found. This is not a guarantee."
                )
            }
        }

        val adviceListTl = when (verdict) {
            RiskVerdict.HIGH_RISK -> listOf(
                "HUWAG i-click ang link o pumunta sa website.",
                "HUWAG magbigay ng OTP, MPIN, o impormasyon ng card.",
                "I-block agad ang nagpadalang numero.",
                "I-report ang numero o link dito sa Tsek."
            )
            RiskVerdict.SUSPICIOUS -> listOf(
                "Huwag munang mag-click o mag-reply.",
                "Buksan ang opisyal na app (tulad ng GCash/BDO) para tingnan kung may tunay na abiso.",
                "Magtanong sa pinagkakatiwalaang kapamilya bago gumawa ng aksyon."
            )
            RiskVerdict.NO_RED_FLAGS -> listOf(
                "Magpatuloy nang may pag-iingat.",
                "Huwag pa rin ibibigay ang iyong OTP o password kahit kanino.",
                "Direktang buksan ang opisyal na website kung may transaksyon."
            )
        }

        val adviceListEn = when (verdict) {
            RiskVerdict.HIGH_RISK -> listOf(
                "DO NOT click any link or visit the referenced site.",
                "NEVER give your OTP, MPIN, or banking details.",
                "Block the sender's number immediately.",
                "Submit a scam report here in Tsek."
            )
            RiskVerdict.SUSPICIOUS -> listOf(
                "Avoid clicking links or replying immediately.",
                "Check directly inside the official app or website.",
                "Consult a trusted family member before taking action."
            )
            RiskVerdict.NO_RED_FLAGS -> listOf(
                "Proceed with ordinary care.",
                "Never share your OTP or PIN under any circumstances.",
                "Always access your account through the official mobile app."
            )
        }

        return RuleEvaluation(
            verdict = verdict,
            score = finalScore,
            category = category,
            redFlags = redFlags,
            explanationTl = explanationTl,
            explanationEn = explanationEn,
            adviceListTl = adviceListTl,
            adviceListEn = adviceListEn
        )
    }
}
