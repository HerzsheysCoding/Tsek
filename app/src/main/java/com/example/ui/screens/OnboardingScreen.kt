package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserAccountEntity
import com.example.ui.components.HandDrawnCheck
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Ink
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper
import com.example.ui.theme.PassGreen

data class IntroSlide(
    val stepNumber: String,
    val title: String,
    val description: String,
    val bulletPoints: List<String>
)

@Composable
fun OnboardingScreen(
    currentUser: UserAccountEntity?,
    isSubmitting: Boolean,
    onFinish: () -> Unit,
    onSignUp: (email: String, pass: String, name: String, supabaseUrl: String?, supabaseKey: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSlideIndex by remember { mutableIntStateOf(0) }
    var showSignUpForm by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var customSupabaseUrl by remember { mutableStateOf("") }
    var customSupabaseKey by remember { mutableStateOf("") }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    val slides = listOf(
        IntroSlide(
            stepNumber = "1/3",
            title = "Protect against Philippine scams",
            description = "Get clear scam verdicts for GCash, Maya, and bank SMS in seconds.",
            bulletPoints = listOf(
                "Detects lookalike domains (e.g. gcash-verify.xyz)",
                "Catches fake parcel delivery and COD texts",
                "Flag OTP and MPIN theft attempts immediately"
            )
        ),
        IntroSlide(
            stepNumber = "2/3",
            title = "Check directly where scams appear",
            description = "Use the Android share sheet or paste suspicious texts directly.",
            bulletPoints = listOf(
                "Share screenshots from Messenger or Viber",
                "Get plain explanations of every red flag",
                "Export marked-up summaries for family chats"
            )
        ),
        IntroSlide(
            stepNumber = "3/3",
            title = "Save your scan history and sync across devices",
            description = "Keep your verified scam reports secure and easily accessible whenever you change phones.",
            bulletPoints = listOf(
                "Sync your personal scan history and trusted sender lists",
                "Optional cloud sync with encrypted backup storage",
                "Basic checks work offline — an account is completely optional"
            )
        )
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top header with Skip / Close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.example.ui.components.TsekOfficialLogo(
                emblemSize = 28.dp,
                fontSize = 20.sp
            )

            Text(
                text = "Skip to app",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = BallpenBlue,
                modifier = Modifier
                    .clickable { onFinish() }
                    .padding(vertical = 4.dp)
                    .testTag("skip_intro_button")
            )
        }

        HorizontalDivider(color = PadBlue, thickness = 1.dp)

        if (!showSignUpForm) {
            // Slide Presentation View (Minimalist, Left-aligned, Single Column)
            AnimatedContent(
                targetState = currentSlideIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "slide_switch"
            ) { slideIndex ->
                val slide = slides[slideIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (slideIndex == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ui.components.TsekOfficialLogo(
                                emblemSize = 64.dp,
                                fontSize = 32.sp,
                                isStacked = true
                            )
                        }
                    }

                    Text(
                        text = "Introduction · ${slide.stepNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink.copy(alpha = 0.5f)
                    )

                    Text(
                        text = slide.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp,
                        color = Ink
                    )

                    Text(
                        text = slide.description,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Ink.copy(alpha = 0.85f)
                    )

                    Surface(
                        color = Paper,
                        border = BorderStroke(1.dp, PadBlue),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            slide.bulletPoints.forEach { point ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    HandDrawnCheck(
                                        modifier = Modifier.size(18.dp),
                                        color = PassGreen,
                                        animate = false
                                    )
                                    Text(
                                        text = point,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Buttons
            if (currentSlideIndex == slides.size - 1) {
                // Final slide: Primary option is Continue without account, equally visible Sign In option
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onFinish() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BallpenBlue,
                            contentColor = Paper
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_without_account_button")
                    ) {
                        Text(
                            text = "Continue without account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showSignUpForm = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, BallpenBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sign_up_slide_button")
                    ) {
                        Text(
                            text = "Save scan history & create account",
                            color = BallpenBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "← Previous",
                            fontSize = 14.sp,
                            color = Ink.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clickable { currentSlideIndex-- }
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentSlideIndex > 0) {
                        OutlinedButton(
                            onClick = { currentSlideIndex-- },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, PadBlue),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Previous", color = Ink)
                        }
                    }

                    Button(
                        onClick = { currentSlideIndex++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BallpenBlue,
                            contentColor = Paper
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Cloud Sync Account Creation Form
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Save scan history",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Text(
                        text = "← Back",
                        fontSize = 14.sp,
                        color = BallpenBlue,
                        modifier = Modifier.clickable { showSignUpForm = false }
                    )
                }

                Text(
                    text = "Create an optional account to securely back up your scan history and sync across your devices.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Ink.copy(alpha = 0.75f)
                )

                if (currentUser != null) {
                    Surface(
                        color = PadBlue.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, PadBlue),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Currently signed in as:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "${currentUser.fullName} (${currentUser.email})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = currentUser.syncMessage,
                                fontSize = 12.sp,
                                color = if (currentUser.supabaseSynced) PassGreen else Ink.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full name") },
                    placeholder = { Text("e.g. Juan Dela Cruz") },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        focusedBorderColor = BallpenBlue,
                        unfocusedBorderColor = PadBlue,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    placeholder = { Text("name@example.com") },
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        focusedBorderColor = BallpenBlue,
                        unfocusedBorderColor = PadBlue,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("At least 8 characters (letters & digits)") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        focusedBorderColor = BallpenBlue,
                        unfocusedBorderColor = PadBlue,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field"),
                    singleLine = true
                )

                // Optional Custom Supabase connection fields toggle
                Text(
                    text = if (showAdvancedSettings) "Hide custom cloud settings" else "+ Custom cloud project URL & Key (optional)",
                    fontSize = 13.sp,
                    color = BallpenBlue,
                    modifier = Modifier
                        .clickable { showAdvancedSettings = !showAdvancedSettings }
                        .padding(vertical = 2.dp)
                )

                if (showAdvancedSettings) {
                    OutlinedTextField(
                        value = customSupabaseUrl,
                        onValueChange = { customSupabaseUrl = it },
                        label = { Text("Cloud project URL") },
                        placeholder = { Text("https://your-project.supabase.co") },
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Paper,
                            unfocusedContainerColor = Paper,
                            focusedBorderColor = BallpenBlue,
                            unfocusedBorderColor = PadBlue,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customSupabaseKey,
                        onValueChange = { customSupabaseKey = it },
                        label = { Text("Public anon key (NOT service_role)") },
                        placeholder = { Text("eyJhbGciOi...") },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Paper,
                            unfocusedContainerColor = Paper,
                            focusedBorderColor = BallpenBlue,
                            unfocusedBorderColor = PadBlue,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            onSignUp(
                                email.trim(),
                                password,
                                fullName.trim().ifBlank { "User" },
                                customSupabaseUrl.trim().ifBlank { null },
                                customSupabaseKey.trim().ifBlank { null }
                            )
                        }
                    },
                    enabled = email.isNotBlank() && password.length >= 8 && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BallpenBlue,
                        contentColor = Paper
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("connect_supabase_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Paper,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Connecting...")
                    } else {
                        Text("Connect and continue", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Continue as guest without cloud sync",
                        fontSize = 14.sp,
                        color = Ink.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { onFinish() }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
