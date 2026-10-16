package com.example.engine

import android.graphics.Bitmap
import com.example.data.model.CheckResult
import com.example.data.model.ExtractedEntities
import com.example.data.model.RiskVerdict
import com.example.data.model.ScamCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ScamDetector {

    suspend fun analyze(
        input: String,
        bitmap: Bitmap? = null,
        inputType: String = "TEXT"
    ): CheckResult = withContext(Dispatchers.Default) {
        val normalizedInput = input.trim()

        // 1. Extract Entities
        val entities = EntityExtractor.extract(normalizedInput)

        // 2. Layer 1: Reputation Checks
        val reputationResults = entities.urls.map { ReputationEngine.checkUrl(it) }

        // 3. Layer 2: Rule Engine
        val ruleResult = RuleEngine.evaluate(normalizedInput, entities, reputationResults)

        // 4. Layer 3: Gemini Analysis (with Privacy Masking applied)
        val sanitizedForCloud = EntityExtractor.sanitizeSensitiveData(normalizedInput)
        val geminiResult = try {
            GeminiScamAnalyzer.analyze(
                text = sanitizedForCloud,
                bitmap = bitmap,
                entities = entities,
                ruleScore = ruleResult.score
            )
        } catch (_: Exception) {
            null
        }

        // 5. Score Fusion
        // Critical conditions always force HIGH_RISK
        val hasCriticalFlag = ruleResult.redFlags.any { it.isCritical }

        val finalScore: Int
        val finalVerdict: RiskVerdict
        val finalCategory: ScamCategory
        val finalExplanationTl: String
        val finalExplanationEn: String
        val finalAdviceTl: List<String>
        val finalAdviceEn: List<String>
        val mergedRedFlags = ruleResult.redFlags.toMutableList()

        if (hasCriticalFlag) {
            finalScore = maxOf(ruleResult.score, geminiResult?.score ?: 85, 90)
            finalVerdict = RiskVerdict.HIGH_RISK
            finalCategory = if (ruleResult.category != ScamCategory.UNKNOWN) ruleResult.category else (geminiResult?.category ?: ScamCategory.PHISHING_LINK)
            finalExplanationTl = geminiResult?.explanationTl?.takeIf { it.isNotBlank() } ?: ruleResult.explanationTl
            finalExplanationEn = geminiResult?.explanationEn?.takeIf { it.isNotBlank() } ?: ruleResult.explanationEn
            finalAdviceTl = if (geminiResult?.adviceListTl?.isNotEmpty() == true) geminiResult.adviceListTl else ruleResult.adviceListTl
            finalAdviceEn = if (geminiResult?.adviceListEn?.isNotEmpty() == true) geminiResult.adviceListEn else ruleResult.adviceListEn
        } else if (geminiResult != null) {
            // Weighted combination: 50% Rules, 50% Gemini
            val blendedScore = ((ruleResult.score * 0.45) + (geminiResult.score * 0.55)).toInt().coerceIn(0, 100)
            finalScore = blendedScore
            finalVerdict = when {
                finalScore >= 65 -> RiskVerdict.HIGH_RISK
                finalScore >= 35 -> RiskVerdict.SUSPICIOUS
                else -> RiskVerdict.NO_RED_FLAGS
            }
            finalCategory = if (geminiResult.category != ScamCategory.UNKNOWN) geminiResult.category else ruleResult.category
            finalExplanationTl = geminiResult.explanationTl.ifBlank { ruleResult.explanationTl }
            finalExplanationEn = geminiResult.explanationEn.ifBlank { ruleResult.explanationEn }
            finalAdviceTl = if (geminiResult.adviceListTl.isNotEmpty()) geminiResult.adviceListTl else ruleResult.adviceListTl
            finalAdviceEn = if (geminiResult.adviceListEn.isNotEmpty()) geminiResult.adviceListEn else ruleResult.adviceListEn

            // Add any additional AI flags
            for (aiFlag in geminiResult.redFlags) {
                if (mergedRedFlags.none { it.titleTl.equals(aiFlag.titleTl, true) }) {
                    mergedRedFlags.add(aiFlag)
                }
            }
        } else {
            // Rules only (deterministic fallback)
            finalScore = ruleResult.score
            finalVerdict = ruleResult.verdict
            finalCategory = ruleResult.category
            finalExplanationTl = ruleResult.explanationTl
            finalExplanationEn = ruleResult.explanationEn
            finalAdviceTl = ruleResult.adviceListTl
            finalAdviceEn = ruleResult.adviceListEn
        }

        CheckResult(
            rawInput = normalizedInput,
            inputType = inputType,
            verdict = finalVerdict,
            score = finalScore,
            category = finalCategory,
            explanationTl = finalExplanationTl,
            explanationEn = finalExplanationEn,
            adviceListTl = finalAdviceTl,
            adviceListEn = finalAdviceEn,
            redFlags = mergedRedFlags,
            entities = entities
        )
    }
}
