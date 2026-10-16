package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CheckResult
import com.example.data.model.RiskVerdict
import com.example.ui.components.HandDrawnCheck
import com.example.ui.components.HandDrawnCross
import com.example.ui.components.HandDrawnWavyUnderline
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen

@Composable
fun ResultScreen(
    result: CheckResult,
    language: AppLanguage = AppLanguage.ENGLISH,
    onBack: () -> Unit,
    onOpenEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isTaglish = language == AppLanguage.TAGLISH

    val isScam = result.verdict == RiskVerdict.HIGH_RISK
    val isSuspicious = result.verdict == RiskVerdict.SUSPICIOUS

    val verdictSentence = when {
        isScam -> if (isTaglish) "Malamang scam ito." else "Likely a scam."
        isSuspicious -> if (isTaglish) "Kahina-hinala ito." else "Suspicious warning."
        else -> if (isTaglish) "Walang nakitang red flag." else "No red flags found."
    }

    val verdictSub = when {
        isScam -> if (isTaglish)
            "${result.redFlags.size} red flag ang nakita."
        else
            "${result.redFlags.size} red flag${if (result.redFlags.size == 1) "" else "s"} identified."
        isSuspicious -> if (isTaglish)
            "May mga senyales ng panganib. Mag-ingat."
        else
            "Warning signals detected. Proceed with caution."
        else -> if (isTaglish)
            "Hindi ito garantiya. Mag-ingat pa rin."
        else
            "This is not a guarantee. Stay vigilant."
    }

    val superscriptMap = listOf("¹", "²", "³", "⁴", "⁵")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plain back navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTaglish) "← Bumalik" else "← Back",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BallpenBlue,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 4.dp)
                    .testTag("back_button")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                com.example.ui.components.TsekOfficialEmblem(size = 18.dp)
                Text(
                    text = "Tsek",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Verdict as a Sentence with Hand-Drawn Animated Mark & Confidence Level
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = verdictSentence,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isScam) CheckerRed else if (isSuspicious) Color(0xFFB45309) else PassGreen
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Confidence & Category Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val confidenceText = when {
                        result.score >= 80 -> if (isTaglish) "Mataas ang Kumpiyansa (${result.score}%)" else "High Confidence (${result.score}%)"
                        result.score >= 50 -> if (isTaglish) "Katamtamang Kumpiyansa (${result.score}%)" else "Moderate Confidence (${result.score}%)"
                        else -> if (isTaglish) "Mababang Panganib (${result.score}%)" else "Low Risk Index (${result.score}%)"
                    }

                    Surface(
                        color = (if (isScam) CheckerRed else if (isSuspicious) Color(0xFFB45309) else PassGreen).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = confidenceText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isScam) CheckerRed else if (isSuspicious) Color(0xFFB45309) else PassGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        color = PadBlue.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = result.category.getDisplayName(isTaglish),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = verdictSub,
                    fontSize = 14.sp,
                    color = Ink
                )
            }

            Box(modifier = Modifier.padding(start = 12.dp)) {
                if (isScam) {
                    HandDrawnCross(modifier = Modifier.size(42.dp), color = CheckerRed)
                } else if (isSuspicious) {
                    HandDrawnCross(modifier = Modifier.size(42.dp), color = Color(0xFFB45309))
                } else {
                    HandDrawnCheck(modifier = Modifier.size(42.dp), color = PassGreen)
                }
            }
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        // The Marked-Up Paper with Faint Pad Blue Ruled Lines
        Surface(
            color = Paper,
            border = BorderStroke(1.dp, PadBlue),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background ruled lines in Pad blue #D5E1F2
                Canvas(modifier = Modifier.matchParentSize()) {
                    val lineSpacing = 30.dp.toPx()
                    var y = lineSpacing
                    while (y < size.height) {
                        drawLine(
                            color = PadBlue,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineSpacing
                    }
                }

                // Marked-up message content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "\"${result.rawInput}\"",
                        fontSize = 16.sp,
                        lineHeight = 30.sp,
                        color = Ink,
                        fontWeight = FontWeight.Medium
                    )

                    // Visual hand-drawn wavy underline moment
                    if (isScam || isSuspicious) {
                        HandDrawnWavyUnderline(
                            color = if (isScam) CheckerRed else Color(0xFFB45309),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(8.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        // Footnotes List with superscript numbers (¹ ² ³)
        if (result.redFlags.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                result.redFlags.forEachIndexed { index, flag ->
                    val superscript = superscriptMap.getOrElse(index) { "•" }
                    val desc = if (isTaglish) {
                        flag.descriptionTl.ifBlank { flag.titleTl }
                    } else {
                        flag.descriptionEn.ifBlank { flag.titleEn }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = superscript,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CheckerRed
                        )
                        Text(
                            text = desc,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Ink
                        )
                    }
                }
            }

            HorizontalDivider(color = PadBlue, thickness = 1.dp)
        }

        // Section: "What to do now" / "Ano ang gagawin" (Plain ordered sequence)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isTaglish) "Ano ang gagawin" else "What to do now",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            val actions = if (isScam) {
                if (isTaglish) {
                    listOf(
                        "1. Huwag i-click ang link.",
                        "2. I-block ang numero.",
                        "3. I-report namin ito para sa iba."
                    )
                } else {
                    listOf(
                        "1. Do not click the link.",
                        "2. Block the sender number.",
                        "3. We will report this to protect others."
                    )
                }
            } else if (isSuspicious) {
                if (isTaglish) {
                    listOf(
                        "1. Huwag munang mag-reply o mag-click.",
                        "2. Buksan ang opisyal na app para i-check.",
                        "3. Magtanong sa pinagkakatiwalaang kapamilya."
                    )
                } else {
                    listOf(
                        "1. Do not reply or send money.",
                        "2. Open the official app directly to verify.",
                        "3. Verify with family or bank support."
                    )
                }
            } else {
                if (isTaglish) {
                    listOf(
                        "1. Magpatuloy nang may pag-iingat.",
                        "2. Huwag pa rin ibibigay ang OTP kailanman.",
                        "3. Direktang makipag-ugnayan sa opisyal na website."
                    )
                } else {
                    listOf(
                        "1. Proceed with ordinary caution.",
                        "2. Never disclose your OTP or MPIN to anyone.",
                        "3. Verify directly on the official platform."
                    )
                }
            }

            actions.forEach { actionText ->
                Text(
                    text = actionText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Ink
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions: [ Send to family ]  [ I was scammed ]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val markedSummary = buildString {
                        if (isTaglish) {
                            append("Ma, tingnan mo ito mula sa Tsek:\n\n")
                            append("\"${result.rawInput}\"\n\n")
                            append("Hatol: $verdictSentence\n")
                            if (result.redFlags.isNotEmpty()) {
                                result.redFlags.forEachIndexed { i, f ->
                                    val sup = superscriptMap.getOrElse(i) { "•" }
                                    append("$sup ${f.descriptionTl}\n")
                                }
                            }
                            append("\nHuwag i-click at huwag magbigay ng OTP.")
                        } else {
                            append("Check this message from Tsek:\n\n")
                            append("\"${result.rawInput}\"\n\n")
                            append("Verdict: $verdictSentence\n")
                            if (result.redFlags.isNotEmpty()) {
                                result.redFlags.forEachIndexed { i, f ->
                                    val sup = superscriptMap.getOrElse(i) { "•" }
                                    append("$sup ${f.descriptionEn}\n")
                                }
                            }
                            append("\nDo not click and never share your OTP.")
                        }
                    }

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, markedSummary)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(
                        sendIntent,
                        if (isTaglish) "Ipadala sa pamilya" else "Send to family"
                    )
                    context.startActivity(shareIntent)
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BallpenBlue,
                    contentColor = Paper
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("send_to_family_button")
            ) {
                Text(
                    text = if (isTaglish) "Ipadala sa pamilya" else "Send to family",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = onOpenEmergency,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, CheckerRed),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("emergency_button")
            ) {
                Text(
                    text = if (isTaglish) "Na-scam na ako" else "I was scammed",
                    color = CheckerRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Community Reporting & Moat
        var reportedToCommunity by remember { mutableStateOf(false) }
        Surface(
            color = if (reportedToCommunity) PassGreen.copy(alpha = 0.12f) else PadBlue.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, if (reportedToCommunity) PassGreen else PadBlue),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (reportedToCommunity)
                            (if (isTaglish) "Salamat! Naidagdag na sa community blocklist." else "Thank you! Added to community blocklist.")
                        else
                            (if (isTaglish) "Tulungan ang ibang Pinoy" else "Community Protection Moat"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = if (isTaglish)
                            "I-report ang bagong modus na ito para ma-block sa ibang users."
                        else
                            "Report this tactic to enrich the blocklist and protect others.",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.65f)
                    )
                }

                if (!reportedToCommunity) {
                    Button(
                        onClick = {
                            reportedToCommunity = true
                            android.widget.Toast.makeText(context, "Report received. Added to verification queue.", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BallpenBlue),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (isTaglish) "I-report" else "Report",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Paper
                        )
                    }
                }
            }
        }

        // Feedback: Was this verdict accurate?
        var userFeedbackGiven by remember { mutableStateOf(false) }
        if (!userFeedbackGiven) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTaglish) "Tama ba ang pagsusuring ito?" else "Was this check accurate?",
                    fontSize = 12.sp,
                    color = Ink.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "👍 Oo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BallpenBlue,
                    modifier = Modifier
                        .clickable {
                            userFeedbackGiven = true
                            android.widget.Toast.makeText(context, "Salamat sa feedback!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = "👎 Hindi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CheckerRed,
                    modifier = Modifier
                        .clickable {
                            userFeedbackGiven = true
                            android.widget.Toast.makeText(context, "Salamat sa feedback!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
