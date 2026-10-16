package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.RiskVerdict
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Highlighter
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen

@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    backgroundColor: Color = Paper,
    borderColor: Color = PadBlue,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

@Composable
fun RiskVerdictBadge(
    verdict: RiskVerdict,
    isTaglish: Boolean,
    modifier: Modifier = Modifier
) {
    val (dotColor, containerColor, textColor) = when (verdict) {
        RiskVerdict.HIGH_RISK -> Triple(CheckerRed, CheckerRed.copy(alpha = 0.12f), CheckerRed)
        RiskVerdict.SUSPICIOUS -> Triple(Color(0xFFB45309), Highlighter.copy(alpha = 0.35f), Color(0xFF92400E))
        RiskVerdict.NO_RED_FLAGS -> Triple(PassGreen, PassGreen.copy(alpha = 0.12f), PassGreen)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = verdict.getDisplayName(isTaglish),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LanguageTogglePill(
    currentLanguage: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("language_toggle"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "English",
            fontSize = 13.sp,
            fontWeight = if (currentLanguage == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal,
            color = if (currentLanguage == AppLanguage.ENGLISH) BallpenBlue else Ink.copy(alpha = 0.5f)
        )
        Text(
            text = "·",
            fontSize = 13.sp,
            color = Ink.copy(alpha = 0.3f)
        )
        Text(
            text = "Taglish",
            fontSize = 13.sp,
            fontWeight = if (currentLanguage == AppLanguage.TAGLISH) FontWeight.Bold else FontWeight.Normal,
            color = if (currentLanguage == AppLanguage.TAGLISH) BallpenBlue else Ink.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ReportScamDialog(
    target: String,
    category: String,
    notes: String,
    isTaglish: Boolean,
    onTargetChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Paper,
        titleContentColor = Ink,
        textContentColor = Ink,
        title = {
            Text(
                text = if (isTaglish) "I-report ang scam" else "Report scam entity",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Ink
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isTaglish)
                        "Makatutulong ang iyong report upang balaan ang iba pang Pilipino."
                    else
                        "Your submission helps protect other Filipinos from receiving the same scam.",
                    fontSize = 14.sp,
                    color = Ink.copy(alpha = 0.75f)
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = onTargetChange,
                    label = { Text(if (isTaglish) "Numero o link" else "Phone number or link") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        focusedBorderColor = BallpenBlue,
                        unfocusedBorderColor = PadBlue,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_target_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text(if (isTaglish) "Mga detalye (optional)" else "Additional notes (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        focusedBorderColor = BallpenBlue,
                        unfocusedBorderColor = PadBlue,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_notes_input"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BallpenBlue,
                    contentColor = Paper
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("report_submit_button")
            ) {
                Text(
                    if (isTaglish) "Ipadala" else "Submit",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, PadBlue)
            ) {
                Text(
                    if (isTaglish) "Kanselahin" else "Cancel",
                    color = Ink.copy(alpha = 0.7f)
                )
            }
        }
    )
}
