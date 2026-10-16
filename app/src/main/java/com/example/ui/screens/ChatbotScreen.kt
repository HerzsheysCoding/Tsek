package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.engine.ChatMessage
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper

@Composable
fun ChatbotScreen(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    language: AppLanguage,
    onSendMessage: (String) -> Unit,
    onOpenEmergency: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isTaglish = language == AppLanguage.TAGLISH

    val context = androidx.compose.ui.platform.LocalContext.current
    fun dial(number: String) {
        val sanitized = number.replace("[^0-9*+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
        context.startActivity(intent)
    }

    val urgentOtpQuestion = if (isTaglish) {
        "Naibigay ko ang OTP ko 19 minutes ago. Ano ang dapat kong gawin agad para maprotektahan ang pera ko?"
    } else {
        "I gave my OTP 19 minutes ago. What should I do right now to protect my money?"
    }

    val regularQuestions = if (isTaglish) {
        listOf(
            "Paano malalaman kung totoo ang SMS ng GCash o bangko?",
            "Scam ba ang mga part-time job task sa Telegram?",
            "Bakit nanghihingi ng delivery fee ang package sa SMS?"
        )
    } else {
        listOf(
            "How do I verify if a GCash or bank SMS is authentic?",
            "How do online product-liker task scams on Telegram work?",
            "Is paying redelivery fees via SMS link safe?"
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Plain back navigation + Emergency SOS Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTaglish) "← Bumalik sa Tsek" else "← Back to Tsek",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BallpenBlue,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 4.dp)
                    .testTag("back_button")
            )

            // Direct SOS Button from Chat
            Surface(
                color = CheckerRed.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CheckerRed.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .clickable { onOpenEmergency() }
                    .testTag("chat_sos_pill")
            ) {
                Text(
                    text = "🚨 SOS 1326",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CheckerRed,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isTaglish) "Tsek AI Assistant" else "Tsek AI Assistant",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        Text(
            text = if (isTaglish)
                "Magtanong tungkol sa kahina-hinalang mensahe, tawag, o online seller."
            else
                "Ask questions about suspicious texts, calls, or unfamiliar online sellers.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Ink.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
        )

        // Important trust disclaimer
        Surface(
            color = Color(0xFFFFFBEB),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ℹ",
                    fontSize = 12.sp,
                    color = Color(0xFFB45309),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isTaglish)
                        "Maaaring magkamali ang AI. Huwag magbigay ng passwords. Kung nakapaglipat na ng pera, pumunta agad sa SOS."
                    else
                        "AI may make mistakes. Never share passwords. If money was already sent, tap SOS immediately.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF92400E)
                )
            }
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        // Chat Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Urgent Emergency Scenario Card (Highly Prominent)
                    Surface(
                        color = CheckerRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.5.dp, CheckerRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSendMessage(urgentOtpQuestion) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTaglish) "⚠️ EMERGENCY SCENARIO" else "⚠️ EMERGENCY SCENARIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CheckerRed
                                )
                                Text(
                                    text = if (isTaglish) "I-tap para itanong →" else "Tap to ask →",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CheckerRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = urgentOtpQuestion,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp,
                                color = Ink
                            )
                        }
                    }

                    Text(
                        text = if (isTaglish) "Iba pang karaniwang tanong:" else "Other common questions:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    regularQuestions.forEach { question ->
                        Surface(
                            color = Paper,
                            border = BorderStroke(1.dp, PadBlue),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSendMessage(question) }
                        ) {
                            Text(
                                text = question,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Ink,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages) { msg ->
                        if (msg.isUser) {
                            // User Message (Right-aligned, bordered)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    color = PadBlue.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, PadBlue),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = Ink,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        } else {
                            // Bot Message (Left-aligned, clean notebook response)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    color = Paper,
                                    border = BorderStroke(1.dp, PadBlue),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Tsek AI",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BallpenBlue,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Text(
                                            text = msg.text,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            color = Ink
                                        )

                                        if (msg.text.contains("EMERGENCY ACTION CHECKLIST")) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(color = CheckerRed.copy(alpha = 0.3f), thickness = 1.dp)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (isTaglish) "Tawagan Ngayon Din:" else "Emergency Actions to Take Right Now:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CheckerRed
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { dial("1326") },
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = CheckerRed),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Call 1326", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paper)
                                                }
                                                Button(
                                                    onClick = { dial("2882") },
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BallpenBlue),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Call GCash", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paper)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedButton(
                                                onClick = onOpenEmergency,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, CheckerRed),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (isTaglish) "Buksan ang Buong SOS Directory →" else "Open Full SOS Emergency Directory →",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CheckerRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = BallpenBlue,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTaglish) "Nag-iisip..." else "Analyzing...",
                                    fontSize = 14.sp,
                                    color = Ink.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        // Input Field + Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (isTaglish) "Magtanong dito..." else "Ask a question...",
                        fontSize = 15.sp,
                        color = Ink.copy(alpha = 0.45f)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Paper,
                    unfocusedContainerColor = Paper,
                    focusedBorderColor = BallpenBlue,
                    unfocusedBorderColor = PadBlue,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Ink
                ),
                singleLine = false,
                maxLines = 3
            )

            Button(
                onClick = {
                    val toSend = inputText.trim()
                    if (toSend.isNotBlank() && !isGenerating) {
                        onSendMessage(toSend)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !isGenerating,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BallpenBlue,
                    contentColor = Paper,
                    disabledContainerColor = Color(0xFF6B8DB5), // Clear, dark, WCAG-compliant contrast so users see it is a button!
                    disabledContentColor = Color.White.copy(alpha = 0.95f)
                ),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("chat_send_button")
            ) {
                Text(
                    text = if (isTaglish) "Ipadala" else "Send",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
