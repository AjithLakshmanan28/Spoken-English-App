package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PronunciationScoreResult
import com.example.audio.SpeechRecognizerHelper
import com.example.audio.SpeechScorer
import com.example.audio.TtsManager
import com.example.data.local.SavedPhrase
import com.example.data.local.SessionRecord
import com.example.data.local.SpokenEnglishDatabase
import com.example.data.model.ConversationMessage
import com.example.data.model.DailyPhrase
import com.example.data.model.DailyPhraseRepository
import com.example.data.model.PronunciationDrill
import com.example.data.model.PronunciationDrillRepository
import com.example.data.model.Scenario
import com.example.data.model.ScenarioRepository
import com.example.data.model.TongueTwister
import com.example.data.model.TongueTwisterRepository
import com.example.data.model.VoiceSettings
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    SCENARIOS,
    ROLEPLAY,
    PRONUNCIATION,
    DAILY_PHRASES,
    SAVED_PHRASES,
    PROGRESS
}

class SpokenEnglishViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SpokenEnglishDatabase.getDatabase(application)
    private val sessionDao = database.sessionDao()
    private val savedPhraseDao = database.savedPhraseDao()
    private val geminiService = GeminiService()

    // TTS & Speech helpers
    private var ttsManager: TtsManager? = null
    private var speechHelper: SpeechRecognizerHelper? = null

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.SCENARIOS)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Scenario & Chat
    private val _activeScenario = MutableStateFlow<Scenario?>(null)
    val activeScenario: StateFlow<Scenario?> = _activeScenario.asStateFlow()

    private val _messages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val messages: StateFlow<List<ConversationMessage>> = _messages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Audio / Speech State
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _liveSpeechTranscript = MutableStateFlow("")
    val liveSpeechTranscript: StateFlow<String> = _liveSpeechTranscript.asStateFlow()

    private val _audioRmsDb = MutableStateFlow(0f)
    val audioRmsDb: StateFlow<Float> = _audioRmsDb.asStateFlow()

    private val _playingMessageId = MutableStateFlow<String?>(null)
    val playingMessageId: StateFlow<String?> = _playingMessageId.asStateFlow()

    private val _voiceSettings = MutableStateFlow(VoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    // Pronunciation Clinic State
    private val _drillCategoryIndex = MutableStateFlow(0) // 0: Sound Drills, 1: Tongue Twisters
    val drillCategoryIndex: StateFlow<Int> = _drillCategoryIndex.asStateFlow()

    private val _currentDrillIndex = MutableStateFlow(0)
    val currentDrillIndex: StateFlow<Int> = _currentDrillIndex.asStateFlow()

    private val _currentTwisterIndex = MutableStateFlow(0)
    val currentTwisterIndex: StateFlow<Int> = _currentTwisterIndex.asStateFlow()

    private val _drillTranscript = MutableStateFlow("")
    val drillTranscript: StateFlow<String> = _drillTranscript.asStateFlow()

    private val _drillScore = MutableStateFlow<PronunciationScoreResult?>(null)
    val drillScore: StateFlow<PronunciationScoreResult?> = _drillScore.asStateFlow()

    private val _isDrillListening = MutableStateFlow(false)
    val isDrillListening: StateFlow<Boolean> = _isDrillListening.asStateFlow()

    // Daily Phrases
    private val _selectedDailyPhraseIndex = MutableStateFlow(0)
    val selectedDailyPhraseIndex: StateFlow<Int> = _selectedDailyPhraseIndex.asStateFlow()

    // Room Persistent Streams
    val savedPhrases: StateFlow<List<SavedPhrase>> = savedPhraseDao.getAllSavedPhrases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessionRecords: StateFlow<List<SessionRecord>> = sessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSessionsCount: StateFlow<Int> = sessionDao.getSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val averageScore: StateFlow<Double?> = sessionDao.getAverageScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        initTts()
    }

    private fun initTts() {
        ttsManager = TtsManager(getApplication()) { success ->
            if (success) {
                ttsManager?.applyVoiceSettings(_voiceSettings.value)
            }
        }
        ttsManager?.onSpeakingStarted = {
            // Already tracked
        }
        ttsManager?.onSpeakingFinished = {
            _playingMessageId.value = null
        }
    }

    fun navigateTo(screen: AppScreen) {
        stopTts()
        stopListening()
        _currentScreen.value = screen
    }

    fun startScenario(scenario: Scenario) {
        _activeScenario.value = scenario
        val initialGreeting = ConversationMessage(
            isUser = false,
            text = scenario.initialAiGreeting,
            suggestedReplies = scenario.starterPhrases
        )
        _messages.value = listOf(initialGreeting)
        _currentScreen.value = AppScreen.ROLEPLAY
        if (_voiceSettings.value.autoPlayAiTts) {
            playTts(initialGreeting.id, scenario.initialAiGreeting)
        }
    }

    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        stopTts()
        _liveSpeechTranscript.value = ""

        val userMessage = ConversationMessage(
            isUser = true,
            text = trimmed
        )
        _messages.value = _messages.value + userMessage
        _isAiThinking.value = true

        val scenario = _activeScenario.value ?: ScenarioRepository.scenarios.first()

        viewModelScope.launch {
            try {
                val aiResponse = geminiService.getConversationTurn(
                    scenario = scenario,
                    history = _messages.value,
                    userUtterance = trimmed
                )

                val aiMessage = ConversationMessage(
                    isUser = false,
                    text = aiResponse.reply,
                    feedback = aiResponse.feedback,
                    nativeAlternative = aiResponse.nativeAlternative,
                    pronunciationTip = aiResponse.pronunciationTip,
                    suggestedReplies = aiResponse.suggestedReplies
                )

                _messages.value = _messages.value + aiMessage
                _isAiThinking.value = false

                if (_voiceSettings.value.autoPlayAiTts) {
                    playTts(aiMessage.id, aiResponse.reply)
                }
            } catch (e: Exception) {
                _isAiThinking.value = false
                val fallbackMsg = ConversationMessage(
                    isUser = false,
                    text = "That's a great thought! Could you tell me a little more about that?",
                    suggestedReplies = scenario.starterPhrases
                )
                _messages.value = _messages.value + fallbackMsg
            }
        }
    }

    fun playTts(messageId: String, text: String) {
        if (_playingMessageId.value == messageId) {
            stopTts()
            return
        }
        _playingMessageId.value = messageId
        ttsManager?.speak(text, messageId)
    }

    fun stopTts() {
        ttsManager?.stop()
        _playingMessageId.value = null
    }

    fun startListeningForRoleplay() {
        _speechError.value = null
        stopTts()
        stopListening()

        speechHelper = SpeechRecognizerHelper(
            context = getApplication(),
            onListeningStarted = {
                _isListening.value = true
                _liveSpeechTranscript.value = ""
            },
            onRmsChanged = { rms ->
                _audioRmsDb.value = rms
            },
            onPartialResult = { partial ->
                _liveSpeechTranscript.value = partial
            },
            onFinalResult = { finalTranscript ->
                _isListening.value = false
                _liveSpeechTranscript.value = finalTranscript
                sendUserMessage(finalTranscript)
            },
            onError = { errMsg ->
                _isListening.value = false
                _speechError.value = errMsg
            }
        )
        speechHelper?.startListening()
    }

    fun stopListening() {
        speechHelper?.stopListening()
        speechHelper?.destroy()
        speechHelper = null
        _isListening.value = false
    }

    fun clearSpeechError() {
        _speechError.value = null
    }

    // Pronunciation Clinic methods
    fun setDrillCategory(index: Int) {
        _drillCategoryIndex.value = index
        _drillScore.value = null
        _drillTranscript.value = ""
        stopListening()
        stopTts()
    }

    fun nextDrill() {
        val total = PronunciationDrillRepository.drills.size
        _currentDrillIndex.value = (_currentDrillIndex.value + 1) % total
        _drillScore.value = null
        _drillTranscript.value = ""
        stopListening()
        stopTts()
    }

    fun prevDrill() {
        val total = PronunciationDrillRepository.drills.size
        _currentDrillIndex.value = if (_currentDrillIndex.value > 0) _currentDrillIndex.value - 1 else total - 1
        _drillScore.value = null
        _drillTranscript.value = ""
        stopListening()
        stopTts()
    }

    fun nextTwister() {
        val total = TongueTwisterRepository.twisters.size
        _currentTwisterIndex.value = (_currentTwisterIndex.value + 1) % total
        _drillScore.value = null
        _drillTranscript.value = ""
        stopListening()
        stopTts()
    }

    fun prevTwister() {
        val total = TongueTwisterRepository.twisters.size
        _currentTwisterIndex.value = if (_currentTwisterIndex.value > 0) _currentTwisterIndex.value - 1 else total - 1
        _drillScore.value = null
        _drillTranscript.value = ""
        stopListening()
        stopTts()
    }

    fun startListeningForDrill(targetPhrase: String) {
        _speechError.value = null
        stopTts()
        stopListening()

        speechHelper = SpeechRecognizerHelper(
            context = getApplication(),
            onListeningStarted = {
                _isDrillListening.value = true
                _drillTranscript.value = ""
                _drillScore.value = null
            },
            onRmsChanged = { rms ->
                _audioRmsDb.value = rms
            },
            onPartialResult = { partial ->
                _drillTranscript.value = partial
            },
            onFinalResult = { finalTranscript ->
                _isDrillListening.value = false
                _drillTranscript.value = finalTranscript
                val scoreResult = SpeechScorer.evaluate(targetPhrase, finalTranscript)
                _drillScore.value = scoreResult
            },
            onError = { errMsg ->
                _isDrillListening.value = false
                _speechError.value = errMsg
            }
        )
        speechHelper?.startListening()
    }

    fun stopListeningForDrill() {
        speechHelper?.stopListening()
        speechHelper?.destroy()
        speechHelper = null
        _isDrillListening.value = false
    }

    fun manualEvaluateDrill(targetPhrase: String, typedText: String) {
        if (typedText.isBlank()) return
        _drillTranscript.value = typedText
        _drillScore.value = SpeechScorer.evaluate(targetPhrase, typedText)
    }

    // Daily Phrases
    fun selectDailyPhrase(index: Int) {
        _selectedDailyPhraseIndex.value = index
        stopTts()
    }

    // Database Actions
    fun savePhrase(
        phrase: String,
        phonetic: String = "",
        meaningOrContext: String = "",
        nativeAlternative: String? = null
    ) {
        viewModelScope.launch {
            savedPhraseDao.insertPhrase(
                SavedPhrase(
                    phrase = phrase,
                    phonetic = phonetic,
                    meaningOrContext = meaningOrContext,
                    nativeAlternative = nativeAlternative
                )
            )
        }
    }

    fun deleteSavedPhrase(id: Long) {
        viewModelScope.launch {
            savedPhraseDao.deletePhrase(id)
        }
    }

    fun finishAndSaveSession(fluencyScore: Int = 88) {
        val scenario = _activeScenario.value ?: return
        val userTurnCount = _messages.value.count { it.isUser }
        if (userTurnCount == 0) {
            _currentScreen.value = AppScreen.SCENARIOS
            return
        }

        viewModelScope.launch {
            sessionDao.insertSession(
                SessionRecord(
                    scenarioId = scenario.id,
                    scenarioTitle = scenario.title,
                    turnCount = userTurnCount,
                    fluencyScore = fluencyScore,
                    summaryFeedback = "Completed $userTurnCount speaking exchanges in ${scenario.title}."
                )
            )
            _currentScreen.value = AppScreen.PROGRESS
        }
    }

    fun updateVoiceSettings(newSettings: VoiceSettings) {
        _voiceSettings.value = newSettings
        ttsManager?.applyVoiceSettings(newSettings)
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        ttsManager?.shutdown()
    }
}
