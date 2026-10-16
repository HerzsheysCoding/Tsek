package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen

@Composable
fun EmergencyScreen(
    onBack: () -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isTaglish = language == AppLanguage.TAGLISH

    fun dial(number: String) {
        val sanitized = number.replace("[^0-9*+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
        context.startActivity(intent)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Plain back link
        Text(
            text = if (isTaglish) "← Bumalik sa Tsek" else "← Back to Tsek",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BallpenBlue,
            modifier = Modifier
                .clickable { onBack() }
                .padding(top = 8.dp, bottom = 4.dp)
                .testTag("back_button")
        )

        // Clean Headline (No decoration, no hero cards)
        Text(
            text = if (isTaglish) "Na-scam na ako" else "I've been scammed",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CheckerRed
        )

        Text(
            text = if (isTaglish)
                "Huwag mag-alala o mahiya. Sundin ang apat na hakbang na ito ngayon mismo upang maprotektahan ang natitirang pera."
            else
                "Do not panic or feel embarrassed. Follow these four urgent steps right now to protect remaining funds.",
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = Ink
        )

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        // Calm Ordered Steps - Fast action first
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EmergencyStepRow(
                number = "1.",
                title = if (isTaglish) "Tawagan o i-lock agad ang e-wallet o bank account." else "Immediately call or lock your e-wallet / bank.",
                detail = if (isTaglish)
                    "Buksan agad ang GCash, Maya, o banking app para palitan ang MPIN/password at i-freeze ang card, o tumawag agad sa hotline."
                else
                    "Open GCash, Maya, or your banking app to freeze cards/MPIN, or call their fraud hotline immediately to stop transfers."
            )

            EmergencyStepRow(
                number = "2.",
                title = if (isTaglish) "Tumawag sa CICC Scam Hotline 1326." else "Call the CICC Scam Hotline 1326.",
                detail = if (isTaglish)
                    "Ang 24/7 hotline ng gobyerno para sa agarang koordinasyon sa mga bangko upang mai-hold ang pinaglipatang account."
                else
                    "Official 24/7 government inter-agency hotline that coordinates with banks to freeze scam remittances."
            )

            EmergencyStepRow(
                number = "3.",
                title = if (isTaglish) "Kunan ng screenshot ang lahat ng ebidensya." else "Screenshot and document all evidence.",
                detail = if (isTaglish)
                    "I-save ang reference number, cellphone number ng scammer, oras, at usapan bago nila burahin."
                else
                    "Save reference numbers, suspect sender IDs, timestamps, and chat transcripts before they get deleted."
            )

            EmergencyStepRow(
                number = "4.",
                title = if (isTaglish) "Mag-file ng blotter sa pulisya." else "File an official police report.",
                detail = if (isTaglish)
                    "Pumunta sa pinakamalapit na PNP Anti-Cybercrime Group (PNP-ACG) upang magkaroon ng opisyal na police report."
                else
                    "Contact the nearest PNP Anti-Cybercrime Group (PNP-ACG) detachment to obtain a certified blotter report."
            )
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        // Verifiable Hotline Directory with Copy & Direct Dial Actions
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isTaglish) "Mga opisyal na numero na matatawagan ngayon" else "Official emergency hotlines to call now",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (isTaglish) "Source: mga opisyal na website, sinuri noong Oktubre 2026" else "Source: official websites, checked Oct 2026",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ink.copy(alpha = 0.65f)
            )
        }

        fun copyNumber(num: String, label: String) {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, num)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "Copied $num ($label)", android.widget.Toast.LENGTH_SHORT).show()
        }

        val checkedSourceText = if (isTaglish) "Source: opisyal na website, sinuri noong Okt 2026" else "Source: official website, checked Oct 2026"

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Priority 1: CICC Inter-Agency Scam Hotline (National Lead Agency)
            Surface(
                color = CheckerRed.copy(alpha = 0.08f),
                border = BorderStroke(1.5.dp, CheckerRed),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CICC Scam Hotline 1326",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CheckerRed
                    )
                    Text(
                        text = if (isTaglish) "Opisyal na 24/7 hotline ng gobyerno para pigilan ang remittance" else "Official 24/7 Gov't hotline to freeze scam remittances",
                        fontSize = 12.sp,
                        color = Ink.copy(alpha = 0.8f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "1326",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 1.sp,
                                color = CheckerRed
                            )
                            Text(
                                text = "Toll-free nationwide · $checkedSourceText",
                                fontSize = 11.sp,
                                color = Ink.copy(alpha = 0.6f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { copyNumber("1326", "CICC 1326") },
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, CheckerRed.copy(alpha = 0.5f)),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Copy", fontSize = 12.sp, color = Ink)
                            }
                            Button(
                                onClick = { dial("1326") },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CheckerRed),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Call", fontWeight = FontWeight.Bold, color = Paper)
                            }
                        }
                    }
                }
            }

            // Priority 2: GCash Fraud & Account Freeze
            HotlineCard(
                name = "GCash Fraud Support",
                number = "2882",
                detail = if (isTaglish) "I-freeze agad ang GCash wallet (Dial 2882)" else "Direct GCash hotline to freeze funds (Dial 2882)",
                sourceNote = checkedSourceText,
                onCall = { dial("2882") },
                onCopy = { copyNumber("2882", "GCash") }
            )

            // Priority 3: Maya Fraud & Account Freeze
            HotlineCard(
                name = "Maya Emergency Support",
                number = "*788",
                detail = if (isTaglish) "Toll-free via Smart/TNT (*788) o (02) 8845-7788" else "Toll-free via Smart/TNT (*788) or (02) 8845-7788",
                sourceNote = checkedSourceText,
                onCall = { dial("*788") },
                onCopy = { copyNumber("*788", "Maya") }
            )

            // Priority 4: BDO Fraud Hotline
            HotlineCard(
                name = "BDO Unibank Fraud Hotline",
                number = "(02) 8631-8000",
                detail = if (isTaglish) "Pindutin ang option para sa lost cards at unauthorized access" else "Select lost card / unauthorized transfer option immediately",
                sourceNote = checkedSourceText,
                onCall = { dial("0286318000") },
                onCopy = { copyNumber("0286318000", "BDO") }
            )

            // Priority 5: BPI Fraud Hotline
            HotlineCard(
                name = "BPI 24/7 Contact Center",
                number = "(02) 889-10000",
                detail = if (isTaglish) "Press 2 para sa report of lost card o fraud emergency" else "Press 2 to report fraud or card compromise",
                sourceNote = checkedSourceText,
                onCall = { dial("0288910000") },
                onCopy = { copyNumber("0288910000", "BPI") }
            )

            // Priority 6: PNP Anti-Cybercrime Group
            HotlineCard(
                name = "PNP Anti-Cybercrime Group (ACG)",
                number = "0998-598-8116",
                detail = if (isTaglish) "Para sa opisyal na police blotter at imbestigasyon" else "For formal police reporting and forensic tracking",
                sourceNote = checkedSourceText,
                onCall = { dial("09985988116") },
                onCopy = { copyNumber("09985988116", "PNP-ACG") }
            )
        }

        // Generous bottom padding to ensure the last card is never obscured by navigation bar
        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
private fun HotlineCard(
    name: String,
    number: String,
    detail: String,
    sourceNote: String,
    onCall: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        color = Paper,
        border = BorderStroke(1.dp, PadBlue),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = detail,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Ink.copy(alpha = 0.75f)
            )

            // High-contrast, large phone number display with clear monospace digits (distinct zeros and eights)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = number,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.8.sp,
                        color = BallpenBlue
                    )
                    Text(
                        text = sourceNote,
                        fontSize = 11.sp,
                        color = Ink.copy(alpha = 0.6f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, PadBlue),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    }

                    Button(
                        onClick = onCall,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BallpenBlue),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paper)
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyStepRow(
    number: String,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CheckerRed
        )
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Ink.copy(alpha = 0.8f)
            )
        }
    }
}
