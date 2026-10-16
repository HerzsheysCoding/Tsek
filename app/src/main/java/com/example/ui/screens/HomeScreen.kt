package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CheckEntity
import com.example.data.model.AppLanguage
import com.example.data.model.RiskVerdict
import com.example.data.model.TrendingAlert
import com.example.ui.TsekUiState
import com.example.ui.TsekViewModel
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.RiskVerdictBadge
import com.example.ui.components.TactileCard
import com.example.ui.components.TsekOfficialLogo
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Highlighter
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: TsekUiState,
    historyList: List<CheckEntity>,
    trendingAlerts: List<TrendingAlert>,
    viewModel: TsekViewModel,
    onOpenEmergency: () -> Unit,
    onOpenChatbot: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenIntroduction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val isTaglish = uiState.language == AppLanguage.TAGLISH

    val sampleScams = remember {
        listOf(
            "Congrats! Nanalo ka ng ₱5,000 mula sa GCash. I-click ang bit.ly/gcash-claim-2024 within 24 hrs at ilagay ang iyong OTP at MPIN para ma-claim agad.",
            "J&T Express: May delivery parcel kayo na may tracking #PH-98231 ngunit kulang ang address. I-download ang app sa parcel-update.xyz/app.apk para ma-deliver.",
            "Part-Time Job: Kumita ng ₱2,500 - ₱5,000 araw-araw sa pag-like ng YouTube videos. Mag-register sa aming Telegram @OnlineHR_Task bago matapos ang slots.",
            "BDO Security Advisory: Ang iyong account ay pansamantalang naka-lock. I-verify ang account sa bdo-security-login.top bago mag-alas 12 ng gabi."
        )
    }
    var sampleIndex by remember { mutableIntStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onScreenshotSelected(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.inputText.isEmpty()) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Official Tsek Logo + Language switcher & SOS Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                TsekOfficialLogo(
                    emblemSize = 36.dp,
                    fontSize = 24.sp
                )
                Text(
                    text = if (isTaglish) "Scam Checker para sa mga Pilipino" else "Philippine Anti-Scam Intelligence",
                    fontSize = 12.sp,
                    color = Ink.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language toggle pill
                LanguageTogglePill(
                    currentLanguage = uiState.language,
                    onToggle = { viewModel.toggleLanguage() }
                )

                // SOS Hotline Pill
                Surface(
                    color = CheckerRed.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CheckerRed.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clickable { onOpenEmergency() }
                        .testTag("sos_emergency_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(CheckerRed, CircleShape)
                        )
                        Text(
                            text = "SOS 1326",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CheckerRed
                        )
                    }
                }
            }
        }

        // Active Threat / Trending Scam Advisory Card
        val topAlert = trendingAlerts.firstOrNull()
        if (topAlert != null) {
            Surface(
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAlerts() }
                    .testTag("trending_alert_banner")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Active Threat",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isTaglish) "Aktibong Modus sa Pilipinas" else "Active Threat Advisory",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }

                        Text(
                            text = if (isTaglish) "Tingnan lahat →" else "View all →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BallpenBlue
                        )
                    }

                    Text(
                        text = if (isTaglish) topAlert.titleTl else topAlert.titleEn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Text(
                        text = "\"${topAlert.sampleMessage}\"",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Ink.copy(alpha = 0.75f),
                        maxLines = 2
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = if (isTaglish) "Subukan sa checker" else "Test in checker",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BallpenBlue,
                            modifier = Modifier
                                .clickable {
                                    viewModel.onInputTextChanged(topAlert.sampleMessage)
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Central Scanner Card
        TactileCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            backgroundColor = Paper,
            borderColor = PadBlue
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTaglish) "Suriin ang mensahe o link" else "Verify message or link",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    if (uiState.inputText.isNotEmpty() || uiState.selectedBitmap != null) {
                        Text(
                            text = if (isTaglish) "Linisin" else "Clear",
                            fontSize = 13.sp,
                            color = CheckerRed,
                            modifier = Modifier
                                .clickable { viewModel.clearInput() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = if (isTaglish)
                        "I-paste ang kahina-hinalang SMS, link, o numero bago mag-click o magpadala ng pera."
                    else
                        "Paste suspicious SMS, link, or number before tapping or transferring money.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Ink.copy(alpha = 0.65f)
                )

                // Input Box
                Surface(
                    color = PadBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PadBlue),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        OutlinedTextField(
                            value = uiState.inputText,
                            onValueChange = { viewModel.onInputTextChanged(it) },
                            placeholder = {
                                Text(
                                    text = if (isTaglish)
                                        "I-paste dito ang mensahe (hal. 'Nanalo ka ng ₱5,000...', bit.ly link, o 09123456789)..."
                                    else
                                        "Paste message here (e.g. 'You won ₱5,000...', bit.ly link, or 09123456789)...",
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = Ink.copy(alpha = 0.45f)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .focusRequester(focusRequester)
                                .testTag("scam_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink,
                                cursorColor = BallpenBlue
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = Ink
                            )
                        )

                        // Attached screenshot preview chip
                        if (uiState.selectedBitmap != null) {
                            Surface(
                                color = Paper,
                                border = BorderStroke(1.dp, PadBlue),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Screenshot",
                                            tint = BallpenBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isTaglish) "Naka-attach ang screenshot" else "Screenshot attached",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Ink
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove screenshot",
                                        tint = CheckerRed,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { viewModel.clearInput() }
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Input Shortcut Pills - 2 Rows to Prevent Any Clipping
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Image,
                            label = "Screenshot",
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            tag = "screenshot_button"
                        )

                        QuickChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ContentPaste,
                            label = if (isTaglish) "I-paste" else "Paste",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = clipboard.primaryClip
                                if (clipData != null && clipData.itemCount > 0) {
                                    val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
                                    if (pasteText.isNotBlank()) {
                                        viewModel.onInputTextChanged(pasteText)
                                    }
                                }
                            },
                            tag = "paste_clipboard_button"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Link,
                            label = "Link (https://)",
                            onClick = {
                                viewModel.onInputTextChanged("https://")
                            },
                            tag = "paste_link_shortcut"
                        )

                        QuickChip(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Phone,
                            label = if (isTaglish) "Numero (09...)" else "Number (09...)",
                            onClick = {
                                viewModel.onInputTextChanged("09")
                            },
                            tag = "paste_number_shortcut"
                        )
                    }

                    // Try Sample Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val sample = sampleScams[sampleIndex % sampleScams.size]
                                viewModel.onInputTextChanged(sample)
                                sampleIndex++
                            }
                            .padding(vertical = 4.dp)
                            .testTag("try_sample_shortcut"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = BallpenBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTaglish) "Subukan ang sample scam message →" else "Try a sample scam message →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BallpenBlue
                        )
                    }
                }

                val hasInput = uiState.inputText.isNotBlank() || uiState.selectedBitmap != null

                // Primary Scan Button (High Contrast WCAG-compliant design with clear state feedback)
                Button(
                    onClick = { viewModel.scanCurrentInput() },
                    enabled = !uiState.isScanning && hasInput,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BallpenBlue,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF6B8DB5), // Solid, distinct, WCAG-compliant contrast so users see it is a button!
                        disabledContentColor = Color.White.copy(alpha = 0.95f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("check_now_button")
                ) {
                    if (uiState.isScanning) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = if (isTaglish) "Sinusuri ng Tsek..." else "Analyzing with Tsek...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Text(
                                text = if (hasInput) {
                                    if (isTaglish) "Suriin Ngayon" else "Check Now"
                                } else {
                                    if (isTaglish) "Mag-paste o mag-attach para suriin" else "Paste or attach to check"
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Recent Checks Section (Focused, direct to task)
        HorizontalDivider(color = PadBlue, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isTaglish) "Mga dating sinuri" else "Recent checks",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            if (historyList.isNotEmpty()) {
                Text(
                    text = if (isTaglish) "Tingnan lahat (${historyList.size}) →" else "View all (${historyList.size}) →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BallpenBlue,
                    modifier = Modifier
                        .clickable { onOpenHistory() }
                        .padding(vertical = 4.dp)
                )
            }
        }

        if (historyList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = PadBlue.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isTaglish) "Wala pang nakaraang pagsusuri." else "No scan history recorded yet.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (isTaglish) "I-paste ang unang mensahe sa itaas para magsimula." else "Paste your first message above to check for scams.",
                            fontSize = 12.sp,
                            color = Ink.copy(alpha = 0.45f)
                        )
                    }
                }

                // Golden Rules shown when history is empty as helpful starter guidance
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, PadBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isTaglish) "3 Gintong Panuntunan Laban sa Panloloko" else "3 Golden Rules to Avoid Scams",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        SafetyRuleItem(
                            number = "1",
                            rule = if (isTaglish)
                                "Huwag ibigay ang OTP o MPIN kahit kanino. Hindi ito hihingin ng GCash o Maya."
                            else
                                "Never share OTP or MPIN with anyone. Legitimate banks will never ask for it."
                        )

                        SafetyRuleItem(
                            number = "2",
                            rule = if (isTaglish)
                                "Huwag mag-click sa mga shortlink (bit.ly, tinyurl) o mag-download ng APK sa SMS."
                            else
                                "Do not tap shortlinks or download unknown APK files received via SMS."
                        )

                        SafetyRuleItem(
                            number = "3",
                            rule = if (isTaglish)
                                "Kung may babayaran bago makuha ang premyo o trabaho, 100% scam ito."
                            else
                                "If you need to pay upfront to claim a prize or job, it is guaranteed a scam."
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                historyList.take(4).forEach { item ->
                    val verdict = when (item.verdict) {
                        "HIGH_RISK" -> RiskVerdict.HIGH_RISK
                        "SUSPICIOUS" -> RiskVerdict.SUSPICIOUS
                        else -> RiskVerdict.NO_RED_FLAGS
                    }

                    val dateFormatted = try {
                        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        sdf.format(Date(item.timestamp))
                    } catch (_: Exception) {
                        ""
                    }

                    Surface(
                        color = Paper,
                        border = BorderStroke(1.dp, PadBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openHistoricalResult(item) }
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
                                    text = item.rawSnippet.take(38),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Ink
                                )
                                if (dateFormatted.isNotBlank()) {
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 11.sp,
                                        color = Ink.copy(alpha = 0.45f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            RiskVerdictBadge(
                                verdict = verdict,
                                isTaglish = isTaglish,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }


        // Cloud Database / About Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.currentUser != null)
                    (if (isTaglish) "Naka-sync sa Cloud Account ✓" else "Synced with Cloud Account ✓")
                else
                    (if (isTaglish) "Tungkol sa Tsek at Cloud Backup →" else "About Tsek & Cloud Backup →"),
                fontSize = 12.sp,
                color = if (uiState.currentUser != null) PassGreen else BallpenBlue,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onOpenIntroduction() }
                    .testTag("open_intro_link")
            )
        }

        // Generous bottom spacer to guarantee last content is clear of navigation bar
        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
private fun QuickChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = Paper,
        border = BorderStroke(1.dp, PadBlue),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = BallpenBlue,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FeatureActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = Paper,
        border = BorderStroke(1.dp, PadBlue),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Ink.copy(alpha = 0.6f),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SafetyRuleItem(
    number: String,
    rule: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = BallpenBlue.copy(alpha = 0.12f),
            shape = CircleShape,
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BallpenBlue
                )
            }
        }

        Text(
            text = rule,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = Ink.copy(alpha = 0.8f)
        )
    }
}
