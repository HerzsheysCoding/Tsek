package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CheckEntity
import com.example.data.local.TsekDatabase
import com.example.data.local.UserAccountEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CheckResult
import com.example.data.model.TrendingAlert
import com.example.data.remote.SupabaseService
import com.example.data.repository.TsekRepository
import com.example.engine.ChatMessage
import com.example.engine.GeminiChatbotService
import com.example.engine.ScamDetector
import com.example.security.FileSecurityManager
import com.example.security.InputSanitizer
import com.example.security.RateLimiter
import com.example.security.SecurityVault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME,
    CHAT,
    ALERTS,
    EMERGENCY,
    HISTORY
}

data class TsekUiState(
    val inputText: String = "",
    val selectedBitmap: Bitmap? = null,
    val selectedBitmapUri: Uri? = null,
    val isScanning: Boolean = false,
    val activeResult: CheckResult? = null,
    val currentTab: AppTab = AppTab.HOME,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val showReportDialog: Boolean = false,
    val reportTarget: String = "",
    val reportCategory: String = "Fake GCash/Maya",
    val reportNotes: String = "",
    val reportSuccessMessage: String? = null,
    // Onboarding & Supabase state
    val isOnboardingCompleted: Boolean = false,
    val isAuthSubmitting: Boolean = false,
    val currentUser: UserAccountEntity? = null,
    // AI Chatbot state
    val chatMessages: List<ChatMessage> = emptyList(),
    val isChatGenerating: Boolean = false
)

class TsekViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TsekRepository
    val historyList: StateFlow<List<CheckEntity>>
    val trendingAlerts: List<TrendingAlert>

    private val _uiState = MutableStateFlow(TsekUiState())
    val uiState: StateFlow<TsekUiState> = _uiState.asStateFlow()

    init {
        val db = TsekDatabase.getDatabase(application)
        repository = TsekRepository(db.checkDao())
        historyList = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        trendingAlerts = repository.getTrendingAlerts()

        // Check if user has an existing account in Room
        viewModelScope.launch {
            repository.userAccount.collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isOnboardingCompleted = true
                    )
                }
            }
        }
    }

    fun completeOnboarding() {
        _uiState.value = _uiState.value.copy(isOnboardingCompleted = true)
    }

    fun restartOnboarding() {
        _uiState.value = _uiState.value.copy(isOnboardingCompleted = false)
    }

    fun signUpToSupabase(
        email: String,
        pass: String,
        fullName: String,
        customUrl: String?,
        customKey: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAuthSubmitting = true)
            val authResult = SupabaseService.registerUser(
                email = email,
                password = pass,
                fullName = fullName,
                customUrl = customUrl,
                customKey = customKey
            )

            if (!authResult.success) {
                _uiState.value = _uiState.value.copy(
                    isAuthSubmitting = false,
                    reportSuccessMessage = authResult.message
                )
                return@launch
            }

            val accountEntity = UserAccountEntity(
                id = authResult.userId,
                email = authResult.email,
                fullName = authResult.fullName,
                supabaseSynced = authResult.isCloudSynced,
                syncMessage = authResult.message
            )

            repository.saveUserAccount(accountEntity)

            _uiState.value = _uiState.value.copy(
                isAuthSubmitting = false,
                currentUser = accountEntity,
                isOnboardingCompleted = true,
                reportSuccessMessage = authResult.message
            )
        }
    }

    fun logOut() {
        viewModelScope.launch {
            repository.clearUserAccount()
            _uiState.value = _uiState.value.copy(
                currentUser = null,
                isOnboardingCompleted = false
            )
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentHistory = _uiState.value.chatMessages
        val userMsg = ChatMessage(isUser = true, text = text)
        val updatedList = currentHistory + userMsg

        _uiState.value = _uiState.value.copy(
            chatMessages = updatedList,
            isChatGenerating = true
        )

        viewModelScope.launch {
            val response = GeminiChatbotService.getChatResponse(
                userMessage = text,
                history = currentHistory,
                isTaglish = _uiState.value.language == AppLanguage.TAGLISH
            )
            val botMsg = ChatMessage(isUser = false, text = response)
            _uiState.value = _uiState.value.copy(
                chatMessages = updatedList + botMsg,
                isChatGenerating = false
            )
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onScreenshotSelected(uri: Uri?) {
        if (uri == null) return
        val context = getApplication<Application>()
        // Point 16: Restrict file uploads (format, size, dimensions, EXIF GPS strip)
        val validation = FileSecurityManager.validateAndProcessImage(context, uri)
        if (!validation.isValid) {
            _uiState.value = _uiState.value.copy(
                reportSuccessMessage = validation.errorMessage ?: "Invalid or unsupported file format."
            )
            return
        }

        val bitmap = validation.sanitizedBitmap
        _uiState.value = _uiState.value.copy(
            selectedBitmap = bitmap,
            selectedBitmapUri = uri,
            inputText = if (_uiState.value.inputText.isBlank()) "[Screenshot Attached]" else _uiState.value.inputText
        )
    }

    fun clearInput() {
        _uiState.value = _uiState.value.copy(
            inputText = "",
            selectedBitmap = null,
            selectedBitmapUri = null
        )
    }

    fun scanCurrentInput() {
        val state = _uiState.value
        val text = state.inputText.trim()
        val bitmap = state.selectedBitmap

        if (text.isBlank() && bitmap == null) return

        // Points 11 & 12: Rate limit scan attempts & bot protection
        val (scanAllowed, rateMsg) = RateLimiter.checkScanAllowed()
        if (!scanAllowed) {
            _uiState.value = _uiState.value.copy(reportSuccessMessage = rateMsg)
            return
        }

        // Points 14 & 15: Validate & escape input content
        if (bitmap == null) {
            val (valid, errorMsg) = InputSanitizer.validateCheckInput(text)
            if (!valid) {
                _uiState.value = _uiState.value.copy(reportSuccessMessage = errorMsg)
                return
            }
        }

        val sanitizedText = InputSanitizer.escapeHtml(text)

        val inputType = when {
            bitmap != null -> "SCREENSHOT"
            text.startsWith("http://") || text.startsWith("https://") -> "LINK"
            text.startsWith("09") || text.startsWith("+63") -> "PHONE"
            else -> "TEXT"
        }

        performScan(sanitizedText, bitmap, inputType)
    }

    fun scanSampleText(sample: String) {
        _uiState.value = _uiState.value.copy(
            inputText = sample,
            selectedBitmap = null,
            selectedBitmapUri = null
        )
        performScan(sample, null, "TEXT")
    }

    private fun performScan(text: String, bitmap: Bitmap?, inputType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val result = ScamDetector.analyze(
                input = text,
                bitmap = bitmap,
                inputType = inputType
            )
            repository.saveCheckResult(result)
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                activeResult = result
            )
        }
    }

    fun handleSharedContent(sharedText: String?, sharedUri: Uri?) {
        if (!sharedText.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                inputText = sharedText,
                currentTab = AppTab.HOME
            )
            performScan(sharedText, null, "SHARE_TEXT")
        } else if (sharedUri != null) {
            onScreenshotSelected(sharedUri)
            _uiState.value = _uiState.value.copy(currentTab = AppTab.HOME)
            scanCurrentInput()
        }
    }

    fun toggleLanguage() {
        val nextLang = if (_uiState.value.language == AppLanguage.TAGLISH) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.TAGLISH
        }
        _uiState.value = _uiState.value.copy(language = nextLang)
    }

    fun switchTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun closeResult() {
        _uiState.value = _uiState.value.copy(activeResult = null)
    }

    fun openHistoricalResult(entity: CheckEntity) {
        val verdict = try {
            com.example.data.model.RiskVerdict.valueOf(entity.verdict)
        } catch (_: Exception) {
            com.example.data.model.RiskVerdict.SUSPICIOUS
        }
        val category = try {
            com.example.data.model.ScamCategory.valueOf(entity.category)
        } catch (_: Exception) {
            com.example.data.model.ScamCategory.UNKNOWN
        }

        val result = CheckResult(
            id = entity.id,
            rawInput = entity.rawSnippet,
            inputType = entity.inputType,
            verdict = verdict,
            score = entity.score,
            category = category,
            explanationTl = entity.explanationTl,
            explanationEn = entity.explanationEn,
            adviceListTl = listOf("Review prior warning.", "Never disclose credentials."),
            adviceListEn = listOf("Review prior warning.", "Never disclose credentials."),
            redFlags = emptyList(),
            entities = com.example.data.model.ExtractedEntities(),
            timestamp = entity.timestamp
        )
        _uiState.value = _uiState.value.copy(activeResult = result)
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteCheck(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun openReportDialog(initialTarget: String = "") {
        _uiState.value = _uiState.value.copy(
            showReportDialog = true,
            reportTarget = initialTarget.ifBlank { _uiState.value.inputText },
            reportSuccessMessage = null
        )
    }

    fun dismissReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = false)
    }

    fun onReportTargetChanged(target: String) {
        _uiState.value = _uiState.value.copy(reportTarget = target)
    }

    fun onReportCategoryChanged(cat: String) {
        _uiState.value = _uiState.value.copy(reportCategory = cat)
    }

    fun onReportNotesChanged(notes: String) {
        _uiState.value = _uiState.value.copy(reportNotes = notes)
    }

    fun submitReport() {
        val state = _uiState.value
        if (state.reportTarget.isBlank()) return
        // Point 15: Escape user content
        val safeTarget = InputSanitizer.escapeHtml(state.reportTarget.trim())
        val safeNotes = InputSanitizer.escapeHtml(state.reportNotes.trim())
        viewModelScope.launch {
            repository.submitReport(
                targetType = "COMMUNITY_REPORT",
                targetValue = safeTarget,
                category = state.reportCategory,
                notes = safeNotes
            )
            _uiState.value = _uiState.value.copy(
                showReportDialog = false,
                reportSuccessMessage = if (state.language == AppLanguage.TAGLISH) {
                    "Salamat! Naitala ang iyong report para mabigyan ng babala ang kapwa Pilipino."
                } else {
                    "Thank you! Your scam report has been submitted to help protect fellow Filipinos."
                }
            )
        }
    }

    fun clearReportSuccessMessage() {
        _uiState.value = _uiState.value.copy(reportSuccessMessage = null)
    }
}
