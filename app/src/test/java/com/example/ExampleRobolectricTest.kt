package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.RiskVerdict
import com.example.data.model.ScamCategory
import com.example.engine.EntityExtractor
import com.example.engine.ReputationEngine
import com.example.engine.RuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Tsek", appName)
    }

    @Test
    fun `test entity extraction on philippine scam message`() {
        val sample = "GCASH ADVISORY: Your account will be blocked within 24 hours. Visit https://gcash-update.xyz to verify. Hotline: 0917-123-4567"
        val entities = EntityExtractor.extract(sample)

        assertTrue(entities.urls.any { it.contains("gcash-update.xyz") })
        assertTrue(entities.phPhoneNumbers.isNotEmpty())
        assertTrue(entities.brands.contains("GCash"))
        assertTrue(entities.urgencyPhrases.isNotEmpty())
    }

    @Test
    fun `test reputation engine detects fake lookalike domain`() {
        val fakeResult = ReputationEngine.checkUrl("https://gcash-verify-update.xyz")
        assertTrue(fakeResult.isImpersonatingPhishing)
        assertEquals("GCASH", fakeResult.impersonatedBrand)
        assertTrue(fakeResult.hasHighRiskTld)

        val legitResult = ReputationEngine.checkUrl("https://www.gcash.com/help")
        assertTrue(legitResult.isOfficialDomain)
        assertTrue(!legitResult.isImpersonatingPhishing)
    }

    @Test
    fun `test rule engine flags OTP theft as critical high risk`() {
        val sample = "Magandang araw! Ibigay po ang inyong OTP o MPIN upang matanggap ang inyong ayuda."
        val entities = EntityExtractor.extract(sample)
        val rep = entities.urls.map { ReputationEngine.checkUrl(it) }
        val eval = RuleEngine.evaluate(sample, entities, rep)

        assertEquals(RiskVerdict.HIGH_RISK, eval.verdict)
        assertEquals(ScamCategory.OTP_THEFT, eval.category)
        assertTrue(eval.redFlags.any { it.isCritical })
    }
}
