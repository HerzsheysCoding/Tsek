package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.TrendingAlert
import com.example.ui.components.RiskVerdictBadge
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Highlighter
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen

@Composable
fun AlertsScreen(
    alerts: List<TrendingAlert>,
    language: AppLanguage,
    onBack: () -> Unit,
    onTestSample: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val isTaglish = language == AppLanguage.TAGLISH

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
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
            )

            Text(
                text = "Tsek Intelligence",
                fontSize = 14.sp,
                color = Ink.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTaglish) "Mga trending na scam" else "Active scam advisories",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }

        Text(
            text = if (isTaglish)
                "Mga pinakabagong ulat ng panloloko sa Pilipinas mula sa PNP-ACG, CICC, BSP, at mga bangko."
            else
                "Verified active scam tactics circulating across Philippine telecom and banking channels.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Ink.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
        )

        // Sort Control Bar
        var sortByNewest by remember { mutableStateOf(true) }
        val displayedAlerts = remember(alerts, sortByNewest) {
            if (sortByNewest) {
                alerts.sortedByDescending { it.publishedDate }
            } else {
                alerts.sortedByDescending { it.reportCount }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTaglish) "Ayusin ayon sa:" else "Sort by:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ink.copy(alpha = 0.6f)
            )

            Surface(
                color = if (sortByNewest) BallpenBlue else Paper,
                border = BorderStroke(1.dp, if (sortByNewest) BallpenBlue else PadBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.clickable { sortByNewest = true }
            ) {
                Text(
                    text = if (isTaglish) "Pinakabago" else "Newest",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sortByNewest) Paper else Ink,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = if (!sortByNewest) BallpenBlue else Paper,
                border = BorderStroke(1.dp, if (!sortByNewest) BallpenBlue else PadBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.clickable { sortByNewest = false }
            ) {
                Text(
                    text = if (isTaglish) "Pinakamaraming Ulat" else "Most Reported",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!sortByNewest) Paper else Ink,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(displayedAlerts) { alert ->
                TrendingAlertItem(
                    alert = alert,
                    isTaglish = isTaglish,
                    onTestSample = onTestSample
                )
            }
        }
    }
}

@Composable
fun TrendingAlertItem(
    alert: TrendingAlert,
    isTaglish: Boolean,
    onTestSample: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = Paper,
        border = BorderStroke(1.dp, PadBlue),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiskVerdictBadge(verdict = alert.severity, isTaglish = isTaglish)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.publishedDate,
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "·",
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (isTaglish) "${alert.reportCount} iniulat sa Tsek" else "${alert.reportCount} reported in Tsek",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BallpenBlue
                    )
                }
            }

            Text(
                text = if (isTaglish) alert.titleTl else alert.titleEn,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            // One-line summary
            Text(
                text = if (isTaglish) alert.howToProtectTl else alert.howToProtectEn,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Ink.copy(alpha = 0.75f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2
            )

            // Verified Source tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${alert.source}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink.copy(alpha = 0.55f)
                )

                Text(
                    text = if (isExpanded) (if (isTaglish) "Itago ▲" else "Hide ▲") else (if (isTaglish) "Tingnan ang detalye ▼" else "See details ▼"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BallpenBlue
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = PadBlue, thickness = 1.dp)

                    Text(
                        text = if (isTaglish) "Halimbawa ng mensahe:" else "Sample scam message:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f)
                    )

                    Surface(
                        color = PadBlue.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${alert.sampleMessage}\"",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Ink,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Text(
                        text = if (isTaglish) "Mga red flag:" else "Identified red flags:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.6f)
                    )

                    val redFlags = if (isTaglish) alert.redFlagsTl else alert.redFlagsEn
                    redFlags.forEach { flag ->
                        Text(
                            text = "• $flag",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Ink
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            color = BallpenBlue,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.clickable { onTestSample(alert.sampleMessage) }
                        ) {
                            Text(
                                text = if (isTaglish) "Subukan sa Checker →" else "Test in Checker →",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Paper,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
