package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.ui.TsekViewModel
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.EmergencyScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.BallpenBlue
import com.example.ui.theme.CheckerRed
import com.example.ui.theme.Ink
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PadBlue
import com.example.ui.theme.Paper

enum class TsekScreen {
    HOME,
    ALERTS,
    CHATBOT,
    HISTORY,
    EMERGENCY,
    ONBOARDING
}

class MainActivity : ComponentActivity() {

    private val viewModel: TsekViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                TsekApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (!sharedText.isNullOrBlank()) {
                    viewModel.handleSharedContent(sharedText = sharedText, sharedUri = null)
                }
            } else if (type.startsWith("image/")) {
                val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                }
                if (imageUri != null) {
                    viewModel.handleSharedContent(sharedText = null, sharedUri = imageUri)
                }
            }
        }
    }
}

@Composable
fun TsekApp(viewModel: TsekViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    val isTaglish = uiState.language == AppLanguage.TAGLISH

    var currentScreen by remember {
        mutableStateOf(if (uiState.isOnboardingCompleted || uiState.currentUser != null) TsekScreen.HOME else TsekScreen.ONBOARDING)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.reportSuccessMessage) {
        val msg = uiState.reportSuccessMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearReportSuccessMessage()
        }
    }

    val activeResult = uiState.activeResult
    val showBottomBar = activeResult == null && currentScreen != TsekScreen.ONBOARDING

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper),
        containerColor = Paper,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Paper,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == TsekScreen.HOME,
                        onClick = { currentScreen = TsekScreen.HOME },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isTaglish) "Home" else "Home",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == TsekScreen.HOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BallpenBlue,
                            selectedTextColor = BallpenBlue,
                            indicatorColor = PadBlue.copy(alpha = 0.5f),
                            unselectedIconColor = Ink.copy(alpha = 0.5f),
                            unselectedTextColor = Ink.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == TsekScreen.ALERTS,
                        onClick = { currentScreen = TsekScreen.ALERTS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alerts",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isTaglish) "Alerto" else "Alerts",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == TsekScreen.ALERTS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BallpenBlue,
                            selectedTextColor = BallpenBlue,
                            indicatorColor = PadBlue.copy(alpha = 0.5f),
                            unselectedIconColor = Ink.copy(alpha = 0.5f),
                            unselectedTextColor = Ink.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_alerts")
                    )

                    NavigationBarItem(
                        selected = currentScreen == TsekScreen.CHATBOT,
                        onClick = { currentScreen = TsekScreen.CHATBOT },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isTaglish) "Tsek AI" else "AI Chat",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == TsekScreen.CHATBOT) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BallpenBlue,
                            selectedTextColor = BallpenBlue,
                            indicatorColor = PadBlue.copy(alpha = 0.5f),
                            unselectedIconColor = Ink.copy(alpha = 0.5f),
                            unselectedTextColor = Ink.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_chat")
                    )

                    NavigationBarItem(
                        selected = currentScreen == TsekScreen.HISTORY,
                        onClick = { currentScreen = TsekScreen.HISTORY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isTaglish) "Kasaysayan" else "History",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == TsekScreen.HISTORY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BallpenBlue,
                            selectedTextColor = BallpenBlue,
                            indicatorColor = PadBlue.copy(alpha = 0.5f),
                            unselectedIconColor = Ink.copy(alpha = 0.5f),
                            unselectedTextColor = Ink.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        selected = currentScreen == TsekScreen.EMERGENCY,
                        onClick = { currentScreen = TsekScreen.EMERGENCY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Emergency",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "SOS 1326",
                                fontSize = 11.sp,
                                fontWeight = if (currentScreen == TsekScreen.EMERGENCY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CheckerRed,
                            selectedTextColor = CheckerRed,
                            indicatorColor = CheckerRed.copy(alpha = 0.15f),
                            unselectedIconColor = CheckerRed.copy(alpha = 0.6f),
                            unselectedTextColor = CheckerRed.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_item_emergency")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
                .padding(innerPadding)
        ) {
            if (activeResult != null) {
                ResultScreen(
                    result = activeResult,
                    language = uiState.language,
                    onBack = { viewModel.closeResult() },
                    onOpenEmergency = {
                        viewModel.closeResult()
                        currentScreen = TsekScreen.EMERGENCY
                    }
                )
            } else {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        TsekScreen.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                historyList = historyList,
                                trendingAlerts = viewModel.trendingAlerts,
                                viewModel = viewModel,
                                onOpenEmergency = { currentScreen = TsekScreen.EMERGENCY },
                                onOpenChatbot = { currentScreen = TsekScreen.CHATBOT },
                                onOpenAlerts = { currentScreen = TsekScreen.ALERTS },
                                onOpenHistory = { currentScreen = TsekScreen.HISTORY },
                                onOpenIntroduction = { currentScreen = TsekScreen.ONBOARDING }
                            )
                        }
                        TsekScreen.ALERTS -> {
                            AlertsScreen(
                                alerts = viewModel.trendingAlerts,
                                language = uiState.language,
                                onBack = { currentScreen = TsekScreen.HOME },
                                onTestSample = { sample ->
                                    viewModel.onInputTextChanged(sample)
                                    currentScreen = TsekScreen.HOME
                                }
                            )
                        }
                        TsekScreen.CHATBOT -> {
                            ChatbotScreen(
                                messages = uiState.chatMessages,
                                isGenerating = uiState.isChatGenerating,
                                language = uiState.language,
                                onSendMessage = { viewModel.sendChatMessage(it) },
                                onOpenEmergency = { currentScreen = TsekScreen.EMERGENCY },
                                onBack = { currentScreen = TsekScreen.HOME }
                            )
                        }
                        TsekScreen.HISTORY -> {
                            HistoryScreen(
                                historyList = historyList,
                                language = uiState.language,
                                onBack = { currentScreen = TsekScreen.HOME },
                                onSelectItem = { item ->
                                    viewModel.openHistoricalResult(item)
                                },
                                onDeleteItem = { id ->
                                    viewModel.deleteHistoryItem(id)
                                },
                                onClearAll = {
                                    viewModel.clearAllHistory()
                                }
                            )
                        }
                        TsekScreen.EMERGENCY -> {
                            EmergencyScreen(
                                onBack = { currentScreen = TsekScreen.HOME },
                                language = uiState.language
                            )
                        }
                        TsekScreen.ONBOARDING -> {
                            OnboardingScreen(
                                currentUser = uiState.currentUser,
                                isSubmitting = uiState.isAuthSubmitting,
                                onFinish = {
                                    viewModel.completeOnboarding()
                                    currentScreen = TsekScreen.HOME
                                },
                                onSignUp = { email, pass, name, url, key ->
                                    viewModel.signUpToSupabase(email, pass, name, url, key)
                                    currentScreen = TsekScreen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
