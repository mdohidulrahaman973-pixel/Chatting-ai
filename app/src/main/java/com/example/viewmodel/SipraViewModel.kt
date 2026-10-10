package com.example.viewmodel

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeminiClient
import com.example.data.MessageSender
import com.example.data.SipraMessage
import com.example.speech.SipraSpeechRecognizer
import com.example.speech.SipraVoiceSynthesizer
import com.example.system.SystemDiagnosticsHelper
import com.example.system.SystemStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SipraStatus {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class SipraViewModel(application: Application) : AndroidViewModel(application) {

    private val diagnosticsHelper = SystemDiagnosticsHelper(application)

    private val _status = MutableStateFlow(SipraStatus.IDLE)
    val status: StateFlow<SipraStatus> = _status.asStateFlow()

    private val _messages = MutableStateFlow<List<SipraMessage>>(emptyList())
    val messages: StateFlow<List<SipraMessage>> = _messages.asStateFlow()

    private val _liveSpeechText = MutableStateFlow("")
    val liveSpeechText: StateFlow<String> = _liveSpeechText.asStateFlow()

    private val _userTitle = MutableStateFlow("Sir")
    val userTitle: StateFlow<String> = _userTitle.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(false)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    // Auto-listen when Lyra stops speaking (no speech into mic during vocalization, auto-turns on immediately after)
    private val _autoListenOnSpeechEnd = MutableStateFlow(true)
    val autoListenOnSpeechEnd: StateFlow<Boolean> = _autoListenOnSpeechEnd.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _systemStats = MutableStateFlow(diagnosticsHelper.getSystemStats())
    val systemStats: StateFlow<SystemStats> = _systemStats.asStateFlow()

    private val _statusNotice = MutableStateFlow("LYRA ONLINE // VOCAL PROTOCOLS ACTIVE")
    val statusNotice: StateFlow<String> = _statusNotice.asStateFlow()

    // Combined amplitude from Speech-to-Text (mic) and Text-to-Speech (voice playback)
    private val _combinedAmplitude = MutableStateFlow(0f)
    val combinedAmplitude: StateFlow<Float> = _combinedAmplitude.asStateFlow()

    private var speechRecognizer: SipraSpeechRecognizer? = null
    private var voiceSynthesizer: SipraVoiceSynthesizer? = null
    private var tickerJob: Job? = null
    private var queryJob: Job? = null

    val availableVoices: StateFlow<List<String>>
        get() = voiceSynthesizer?.availableFemaleVoices ?: MutableStateFlow(emptyList())

    val selectedVoiceName: StateFlow<String>
        get() = voiceSynthesizer?.selectedVoiceName ?: MutableStateFlow("SIPRA Core (Female)")

    val speechPitch: Float
        get() = voiceSynthesizer?.speechPitch ?: 1.10f

    val speechSpeed: Float
        get() = voiceSynthesizer?.speechSpeed ?: 1.04f

    init {
        initializeAudioEngines(application)
        startTelemetryLoop()
        postInitialGreeting()
    }

    private fun initializeAudioEngines(app: Application) {
        speechRecognizer = SipraSpeechRecognizer(
            context = app,
            onSpeechRecognized = { text ->
                _liveSpeechText.value = ""
                processUserSpeech(text)
            },
            onError = { err ->
                _status.value = SipraStatus.IDLE
                _statusNotice.value = err
            },
            onListeningStarted = {
                _status.value = SipraStatus.LISTENING
                _statusNotice.value = "ACOUSTIC SENSOR LISTENING..."
            },
            onEndOfSpeech = {
                _status.value = SipraStatus.PROCESSING
                _statusNotice.value = "ANALYZING ACOUSTIC SIGNAL..."
            },
            onListeningCancelled = {
                _status.value = SipraStatus.IDLE
                _statusNotice.value = "STANDBY // READY FOR DIRECTIVE"
            }
        )

        voiceSynthesizer = SipraVoiceSynthesizer(
            context = app,
            onSpeechStarted = {
                // Ensure microphone input is completely stopped so no audio goes to mic while Lyra speaks
                speechRecognizer?.stopListening()
                _liveSpeechText.value = ""
                _status.value = SipraStatus.SPEAKING
                _statusNotice.value = "LYRA VOCALIZING... // MIC MUTED"
            },
            onSpeechCompleted = {
                _status.value = SipraStatus.IDLE
                _statusNotice.value = "STANDBY // READY FOR DIRECTIVE"
                // As soon as Lyra stops speaking, automatically turn on mic for hands-free conversational flow
                if (_autoListenOnSpeechEnd.value && !_isMuted.value) {
                    viewModelScope.launch {
                        delay(350)
                        startVoiceInput()
                    }
                }
            },
            onSpeechError = { _ ->
                _status.value = SipraStatus.IDLE
                _statusNotice.value = "STANDBY // READY FOR DIRECTIVE"
                if (_autoListenOnSpeechEnd.value && !_isMuted.value) {
                    viewModelScope.launch {
                        delay(350)
                        startVoiceInput()
                    }
                }
            }
        )

        // Combine live mic and TTS playback amplitudes for holographic visuals
        viewModelScope.launch {
            val micAmpFlow = speechRecognizer?.audioAmplitude ?: MutableStateFlow(0f)
            val ttsAmpFlow = voiceSynthesizer?.playbackAmplitude ?: MutableStateFlow(0f)
            combine(micAmpFlow, ttsAmpFlow) { mic, tts ->
                maxOf(mic, tts)
            }.collect { amp ->
                _combinedAmplitude.value = amp
            }
        }

        viewModelScope.launch {
            speechRecognizer?.partialText?.collect { partial ->
                _liveSpeechText.value = partial
            }
        }
    }

    private fun startTelemetryLoop() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                _systemStats.value = diagnosticsHelper.getSystemStats()
                delay(1000)
            }
        }
    }

    private fun postInitialGreeting() {
        val initialGreeting = "Good day, ${_userTitle.value}. All systems initialized and nominal. Voice interface online. I am Lyra, at your command. What is your directive?"
        val welcomeMessage = SipraMessage(
            sender = MessageSender.SIPRA,
            text = initialGreeting
        )
        _messages.value = listOf(welcomeMessage)

        // Speak welcome aloud on launch
        viewModelScope.launch {
            delay(800)
            if (!_isMuted.value) {
                voiceSynthesizer?.speak(initialGreeting)
            }
        }
    }

    fun startVoiceInput() {
        val app = getApplication<Application>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                app,
                android.Manifest.permission.RECORD_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            _status.value = SipraStatus.IDLE
            _statusNotice.value = "ACOUSTIC PERMISSION REQUIRED // GRANT MIC"
            return
        }
        vibrateTactile(30)
        voiceSynthesizer?.stop()
        _status.value = SipraStatus.LISTENING
        _statusNotice.value = "ACOUSTIC SENSOR LISTENING..."
        speechRecognizer?.startListening()
    }

    fun stopVoiceInput() {
        speechRecognizer?.stopListening()
        if (_status.value == SipraStatus.LISTENING) {
            _status.value = SipraStatus.IDLE
            _statusNotice.value = "STANDBY // READY FOR DIRECTIVE"
        }
    }

    fun toggleVoiceInput() {
        when (_status.value) {
            SipraStatus.LISTENING -> stopVoiceInput()
            SipraStatus.SPEAKING, SipraStatus.PROCESSING -> cancelOrInterrupt()
            else -> startVoiceInput()
        }
    }

    fun cancelOrInterrupt() {
        vibrateTactile(25)
        speechRecognizer?.stopListening()
        voiceSynthesizer?.stop()
        queryJob?.cancel()
        _status.value = SipraStatus.IDLE
        _statusNotice.value = "STANDBY // READY FOR DIRECTIVE"
    }

    fun processUserSpeech(userText: String) {
        if (userText.isBlank()) return

        vibrateTactile(40)
        val userMsg = SipraMessage(
            sender = MessageSender.USER,
            text = userText
        )
        _messages.value = _messages.value + userMsg

        _status.value = SipraStatus.PROCESSING
        _statusNotice.value = "NEURAL PROCESSING // QUERYING LYRA CORE..."

        // Check local tactical directives before calling external API if applicable
        val lower = userText.lowercase().trim()
        when {
            lower.contains("diagnostics") || lower.contains("status report") || lower.contains("system status") -> {
                val report = diagnosticsHelper.generateDiagnosticReportString()
                deliverSipraResponse(report)
                return
            }
            lower.contains("torch") || lower.contains("flashlight") -> {
                val shouldTurnOn = !lower.contains("off")
                toggleTorchExplicit(shouldTurnOn)
                val reply = if (shouldTurnOn) {
                    "Tactical photon emitter activated, ${_userTitle.value}."
                } else {
                    "Tactical illumination deactivated, ${_userTitle.value}."
                }
                deliverSipraResponse(reply)
                return
            }
            lower.contains("who are you") || lower.contains("what is your name") -> {
                val identity = "I am Lyra—your personal tactical voice assistant and operating system. I manage your device diagnostics, intelligence retrieval, and speech-to-speech protocols, ${_userTitle.value}."
                deliverSipraResponse(identity)
                return
            }
        }

        // Call Gemini AI Core
        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            val systemDirective = """
                You are Lyra, an advanced Jarvis-style AI operating system and tactical companion for mobile devices.
                You have an intelligent, witty, concise, highly capable, and confident female persona.
                User call-sign: ${_userTitle.value}.
                Rules for interaction:
                1. Always respond respectfully addressing the user as '${_userTitle.value}'.
                2. Be concise, brilliant, and articulate.
                3. Since your output is spoken aloud using speech-to-speech female voice synthesis, keep your answers conversational, natural, and free of markdown clutter, asterisks, or nested bullet points.
                4. For calculations, facts, code questions, advice, or general knowledge, provide the direct answer immediately with a touch of high-tech charm.
            """.trimIndent()

            val result = GeminiClient.generateSipraResponse(
                userPrompt = userText,
                conversationHistory = _messages.value.dropLast(1),
                systemDirective = systemDirective,
                apiKeyOverride = _customApiKey.value.ifBlank { null }
            )

            result.onSuccess { reply ->
                deliverSipraResponse(reply)
            }.onFailure { err ->
                _status.value = SipraStatus.ERROR
                val errMsg = "Neural link anomaly: ${err.message ?: "Unable to contact core."}"
                _statusNotice.value = errMsg
                val errorMsg = SipraMessage(
                    sender = MessageSender.SIPRA,
                    text = errMsg,
                    isError = true
                )
                _messages.value = _messages.value + errorMsg
                if (!_isMuted.value) {
                    voiceSynthesizer?.speak("Apologies, ${_userTitle.value}. ${err.localizedMessage ?: "I encountered an anomaly contacting the neural core."}")
                }
            }
        }
    }

    private fun deliverSipraResponse(reply: String) {
        val sipraMsg = SipraMessage(
            sender = MessageSender.SIPRA,
            text = reply
        )
        _messages.value = _messages.value + sipraMsg

        if (!_isMuted.value) {
            // Stop speech recognition immediately before vocalization
            speechRecognizer?.stopListening()
            _liveSpeechText.value = ""
            _status.value = SipraStatus.SPEAKING
            _statusNotice.value = "LYRA VOCALIZING... // MIC MUTED"
            voiceSynthesizer?.speak(reply)
        } else {
            _status.value = SipraStatus.IDLE
            _statusNotice.value = "STANDBY // READY FOR DIRECTIVE"
            if (_autoListenOnSpeechEnd.value) {
                viewModelScope.launch {
                    delay(350)
                    startVoiceInput()
                }
            }
        }
    }

    fun repeatMessage(message: SipraMessage) {
        if (!_isMuted.value) {
            speechRecognizer?.stopListening()
            _liveSpeechText.value = ""
            _status.value = SipraStatus.SPEAKING
            voiceSynthesizer?.speak(message.text)
        }
    }

    fun setUserTitle(title: String) {
        _userTitle.value = title.trim().ifBlank { "Sir" }
    }

    fun setContinuousMode(enabled: Boolean) {
        _isContinuousMode.value = enabled
    }

    fun setAutoListenOnSpeechEnd(enabled: Boolean) {
        _autoListenOnSpeechEnd.value = enabled
        _statusNotice.value = if (enabled) "AUTO-MIC ACTIVE // LISTENS WHEN VOCAL ENDS" else "AUTO-MIC MANUAL"
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        if (newMute) {
            voiceSynthesizer?.stop()
        }
        _statusNotice.value = if (newMute) "AUDIO CHANNELS MUTED" else "VOCAL CHANNELS ACTIVE"
    }

    fun toggleTorch() {
        val newState = !_isTorchOn.value
        toggleTorchExplicit(newState)
    }

    private fun toggleTorchExplicit(enable: Boolean) {
        val success = diagnosticsHelper.setTorchMode(enable)
        if (success) {
            _isTorchOn.value = enable
        }
    }

    fun setVoicePitch(pitch: Float) {
        voiceSynthesizer?.speechPitch = pitch
    }

    fun setVoiceSpeed(speed: Float) {
        voiceSynthesizer?.speechSpeed = speed
    }

    fun setVoiceByName(voiceName: String) {
        voiceSynthesizer?.setVoiceByName(voiceName)
    }

    fun setCustomApiKey(key: String) {
        val trimmed = key.trim()
        _customApiKey.value = trimmed
        _statusNotice.value = if (trimmed.isNotBlank()) "CUSTOM GEMINI KEY STORED" else "DEFAULT NEURAL LINK ACTIVE"
    }

    fun testApiKey(keyCandidate: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val keyToTest = keyCandidate.trim().ifBlank { _customApiKey.value.ifBlank { null } }
            val systemDirective = "You are Lyra, tactical assistant. Provide a 1-sentence confirmation that neural link is nominal."
            val result = GeminiClient.generateSipraResponse(
                userPrompt = "Neural link ping test. Confirm status.",
                conversationHistory = emptyList(),
                systemDirective = systemDirective,
                apiKeyOverride = keyToTest
            )
            result.onSuccess { reply ->
                onResult(true, reply)
            }.onFailure { err ->
                onResult(false, err.message ?: "Authentication failure")
            }
        }
    }

    fun testVocalEngine() {
        val testText = "Acoustic synthesis check. Lyra voice subsystem operating with precision, ${_userTitle.value}."
        speechRecognizer?.stopListening()
        voiceSynthesizer?.speak(testText)
    }

    fun clearLog() {
        _messages.value = emptyList()
        _statusNotice.value = "DIALOGUE LOG PURGED // CORE CLEAN"
    }

    fun vibrateTactile(durationMs: Long = 40) {
        try {
            val app = getApplication<Application>()
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val manager = app.getSystemService(VibratorManager::class.java)
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                app.getSystemService(Vibrator::class.java)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        queryJob?.cancel()
        speechRecognizer?.destroy()
        voiceSynthesizer?.destroy()
        diagnosticsHelper.setTorchMode(false)
    }
}
