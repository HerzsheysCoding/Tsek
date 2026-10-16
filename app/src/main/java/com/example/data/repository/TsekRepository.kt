package com.example.data.repository

import com.example.data.local.CheckDao
import com.example.data.local.CheckEntity
import com.example.data.local.LocalReportEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.CheckResult
import com.example.data.model.RiskVerdict
import com.example.data.model.ScamCategory
import com.example.data.model.TrendingAlert
import kotlinx.coroutines.flow.Flow

class TsekRepository(private val checkDao: CheckDao) {

    val allHistory: Flow<List<CheckEntity>> = checkDao.getAllChecks()
    val allReports: Flow<List<LocalReportEntity>> = checkDao.getAllReports()
    val userAccount: Flow<UserAccountEntity?> = checkDao.getUserAccount()

    suspend fun saveUserAccount(user: UserAccountEntity) = checkDao.insertUserAccount(user)
    suspend fun clearUserAccount() = checkDao.clearUserAccount()

    suspend fun saveCheckResult(result: CheckResult) {
        val entity = CheckEntity(
            id = result.id,
            rawSnippet = result.rawInput.take(200),
            inputType = result.inputType,
            verdict = result.verdict.name,
            score = result.score,
            category = result.category.name,
            explanationTl = result.explanationTl,
            explanationEn = result.explanationEn,
            redFlagCount = result.redFlags.size,
            primaryDomain = result.entities.urls.firstOrNull(),
            primaryPhone = result.entities.phPhoneNumbers.firstOrNull(),
            timestamp = result.timestamp
        )
        checkDao.insertCheck(entity)
    }

    suspend fun deleteCheck(id: String) {
        checkDao.deleteCheckById(id)
    }

    suspend fun clearHistory() {
        checkDao.clearAllChecks()
    }

    suspend fun submitReport(targetType: String, targetValue: String, category: String, notes: String) {
        val report = LocalReportEntity(
            targetType = targetType,
            targetValue = targetValue,
            category = category,
            notes = notes
        )
        checkDao.insertReport(report)
    }

    fun getTrendingAlerts(): List<TrendingAlert> {
        return listOf(
            TrendingAlert(
                id = "alert_1",
                titleTl = "SIM Registration Deactivation Warning (Pekeng Telco/GCash)",
                titleEn = "Fake SIM Registration Expiry Phishing",
                category = ScamCategory.FAKE_EWALLET,
                severity = RiskVerdict.HIGH_RISK,
                targetBrand = "GCash / Smart / Globe",
                sampleMessage = "ADVISORY: Ang iyong SIM at GCash account ay madi-deactivate sa loob ng 24 oras dahil sa hindi kumpletong SIM registration. Mag-login agad sa https://sim-gcash-update.top upang mapanatili ang iyong account.",
                redFlagsTl = listOf(
                    "Pekeng URL link (.top TLD sa halip na opisyal na .com o app)",
                    "Artificial urgency ('sa loob ng 24 oras')",
                    "Hinihingi ang login credentials sa labas ng opisyal na app"
                ),
                redFlagsEn = listOf(
                    "Fake web domain (.top instead of official website/app)",
                    "Urgency pressure ('within 24 hours')",
                    "Prompts external web login instead of the authentic app"
                ),
                howToProtectTl = "Huwag i-click ang link! Ang totoong GCash at telcos ay hindi nagpapadala ng link via SMS para mag-verify.",
                howToProtectEn = "Never click SMS links. Legitimate telcos and e-wallets do not send clickable external links for re-verification.",
                source = "DICT & CICC Public Scam Advisory",
                publishedDate = "1 day ago",
                reportCount = 389
            ),
            TrendingAlert(
                id = "alert_2",
                titleTl = "Part-Time Shopee / TikTok 'Product Liker' Task Scam",
                titleEn = "Online Task / Product Liker Job Scam",
                category = ScamCategory.FAKE_JOB_TASK,
                severity = RiskVerdict.HIGH_RISK,
                targetBrand = "Shopee / Telegram / TikTok",
                sampleMessage = "Hello po! We are offering a home-based part-time job. Earn PHP 2,000 - PHP 5,000 daily just by liking products on Shopee. Contact our manager on Telegram @EarnDailyPH now!",
                redFlagsTl = listOf(
                    "Masyadong malaking sweldo para sa napakadaling gawain ('PHP 2,000 - 5,000/day')",
                    "Inililipat ang usapan sa Telegram o WhatsApp",
                    "Sa huli ay hihingan ka ng 'recharge' o pera bago mailabas ang kunwaring kita"
                ),
                redFlagsEn = listOf(
                    "Unrealistically high payout for low effort",
                    "Redirection to unofficial chat apps like Telegram or WhatsApp",
                    "Eventually requires 'top-up' or deposit before releasing fake earnings"
                ),
                howToProtectTl = "I-block ang nag-message. Walang lehitimong kumpanya na nagpapasahod ng libo para lang mag-like ng product.",
                howToProtectEn = "Block the sender immediately. Legitimate companies never pay thousands for simple social media likes.",
                source = "PNP Anti-Cybercrime Group (PNP-ACG)",
                publishedDate = "3 days ago",
                reportCount = 215
            ),
            TrendingAlert(
                id = "alert_3",
                titleTl = "Pekeng J&T / LBC Delivery Fee SMS",
                titleEn = "Fake Delivery Fee / Parcel Tracking Link",
                category = ScamCategory.PARCEL_DELIVERY,
                severity = RiskVerdict.SUSPICIOUS,
                targetBrand = "J&T Express / LBC / Flash",
                sampleMessage = "J&T EXPRESS: Hindi ma-deliver ang iyong package #JT-99218 dahil kulang ang address info. Mangyaring magbayad ng PHP 50 handling fee sa loob ng 12 oras sa https://jt-ph-redelivery.xyz",
                redFlagsTl = listOf(
                    "Nanghihingi ng karagdagang bayad sa website para sa package na hindi mo inorder",
                    "Pekeng address link (.xyz)",
                    "Target ang mga taong madalas mag-online shopping"
                ),
                redFlagsEn = listOf(
                    "Charges an unexpected fee on a fake website",
                    "Fake courier URL (.xyz TLD)",
                    "Exploits regular e-commerce buyers"
                ),
                howToProtectTl = "Tingnan ang iyong orders sa mismong Shopee/Lazada app. Huwag magbayad sa kahit anong SMS link.",
                howToProtectEn = "Check order status directly in your official shopping app. Never pay courier fees via SMS links.",
                source = "DTI Consumer Protection Alert",
                publishedDate = "5 days ago",
                reportCount = 184
            ),
            TrendingAlert(
                id = "alert_4",
                titleTl = "Pekeng Bangko 'Suspicious Transaction' Alert",
                titleEn = "Fake Bank Unauthorized Login SMS",
                category = ScamCategory.PHISHING_LINK,
                severity = RiskVerdict.HIGH_RISK,
                targetBrand = "BDO / BPI / UnionBank",
                sampleMessage = "BDO ALERT: A new device accessed your online banking. If this was not you, cancel the transaction immediately by clicking https://bdo-online-cancel.com",
                redFlagsTl = listOf(
                    "Pekeng domain na may 'bdo-online-cancel.com' sa halip na bdo.com.ph",
                    "Tinatakot ang biktima upang mag-panic at mag-login agad",
                    "Ipinapasok ang OTP sa pekeng portal upang makuha ng hacker"
                ),
                redFlagsEn = listOf(
                    "Fake phishing domain pretending to be BDO",
                    "Panic-inducing warning to rush user input",
                    "Harvests online banking username, password, and OTP"
                ),
                howToProtectTl = "Huwag mag-login sa link. Buksan ang iyong opisyal na BDO app o tumawag sa opisyal na hotline ng bangko.",
                howToProtectEn = "Never log in via links. Open your authentic banking app directly or call official bank hotline.",
                source = "Bangko Sentral ng Pilipinas (BSP) Advisory",
                publishedDate = "1 week ago",
                reportCount = 512
            )
        )
    }
}
