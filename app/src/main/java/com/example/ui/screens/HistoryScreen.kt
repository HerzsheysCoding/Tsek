package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CheckEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<CheckEntity>,
    language: AppLanguage,
    onBack: () -> Unit,
    onSelectItem: (CheckEntity) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val isTaglish = language == AppLanguage.TAGLISH
    var showClearDialog by remember { mutableStateOf(false) }

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

            if (historyList.isNotEmpty()) {
                Text(
                    text = if (isTaglish) "Burahin lahat" else "Clear all",
                    fontSize = 14.sp,
                    color = CheckerRed,
                    modifier = Modifier.clickable { showClearDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isTaglish) "Kasaysayan ng pagsusuri" else "Check history",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Text(
            text = if (isTaglish)
                "Nananatiling ligtas at lokal sa iyong device ang lahat ng pagsusuri."
            else
                "All analyses are kept private and stored locally on your device.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Ink.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isTaglish) "Wala pang nakaraang pagsusuri." else "No scan history recorded yet.",
                    fontSize = 15.sp,
                    color = Ink.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList) { item ->
                    val isScam = item.verdict == "HIGH_RISK"
                    val isSuspicious = item.verdict == "SUSPICIOUS"
                    val labelColor = if (isScam) CheckerRed else if (isSuspicious) Color(0xFFB45309) else PassGreen
                    val dateFormatted = try {
                        val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                        sdf.format(Date(item.timestamp))
                    } catch (_: Exception) {
                        ""
                    }

                    Surface(
                        color = Paper,
                        border = BorderStroke(1.dp, PadBlue),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectItem(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.rawSnippet.take(50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                                if (dateFormatted.isNotBlank()) {
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 12.sp,
                                        color = Ink.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = if (isScam)
                                    (if (isTaglish) "Scam" else "Scam")
                                else if (isSuspicious)
                                    (if (isTaglish) "Kahina-hinala" else "Suspicious")
                                else
                                    (if (isTaglish) "Walang Red Flag" else "No Red Flags"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = labelColor,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = Paper,
            titleContentColor = Ink,
            textContentColor = Ink,
            title = {
                Text(
                    text = if (isTaglish) "Burahin ang kasaysayan?" else "Clear check history?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (isTaglish)
                        "Mabubura ang lahat ng nakaimbak na tala ng pagsusuri sa iyong telepono."
                    else
                        "This will permanently remove all past checks saved on this device.",
                    fontSize = 14.sp,
                    color = Ink.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CheckerRed,
                        contentColor = Paper
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(if (isTaglish) "Burahin" else "Clear")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    border = BorderStroke(1.dp, PadBlue),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(if (isTaglish) "Kanselahin" else "Cancel", color = Ink)
                }
            }
        )
    }
}
