package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class SipraVoiceSynthesizer(
    private val context: Context,
    private val onSpeechStarted: () -> Unit,
    private val onSpeechCompleted: () -> Unit,
    private val onSpeechError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var visualizerJob: Job? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _playbackAmplitude = MutableStateFlow(0f)
    val playbackAmplitude: StateFlow<Float> = _playbackAmplitude.asStateFlow()

    private val _availableFemaleVoices = MutableStateFlow<List<String>>(emptyList())
    val availableFemaleVoices: StateFlow<List<String>> = _availableFemaleVoices.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow("Lyra Core (Female)")
    val selectedVoiceName: StateFlow<String> = _selectedVoiceName.asStateFlow()

    var speechPitch: Float = 1.10f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setPitch(field)
        }

    var speechSpeed: Float = 1.04f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setSpeechRate(field)
        }

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }

            tts?.setPitch(speechPitch)
            tts?.setSpeechRate(speechSpeed)

            configureFemaleVoice()
            setupUtteranceListener()
            isInitialized = true
        } else {
            onSpeechError("Voice synthesis engine initialization failed.")
        }
    }

    private fun configureFemaleVoice() {
        try {
            val voices = tts?.voices ?: return
            val femaleVoices = voices.filter { voice ->
                val name = voice.name.lowercase(Locale.ROOT)
                val isEnglish = voice.locale.language.equals("en", ignoreCase = true)
                val matchesFemale = name.contains("female") ||
                        name.contains("f0") ||
                        name.contains("woman") ||
                        name.contains("sfg") ||
                        name.contains("fem") ||
                        name.contains("girl")
                isEnglish && matchesFemale && !voice.isNetworkConnectionRequired
            }

            val candidateList = if (femaleVoices.isNotEmpty()) femaleVoices else {
                voices.filter { it.locale.language.equals("en", ignoreCase = true) }
            }

            val voiceNames = candidateList.map { it.name }
            _availableFemaleVoices.value = voiceNames

            // Select the best female voice candidate
            val bestVoice = femaleVoices.firstOrNull() ?: candidateList.firstOrNull()
            if (bestVoice != null) {
                tts?.voice = bestVoice
                _selectedVoiceName.value = bestVoice.name
            }
        } catch (_: Exception) {
            // Default locale voice fallback
        }
    }

    fun setVoiceByName(voiceName: String) {
        try {
            val targetVoice = tts?.voices?.firstOrNull { it.name == voiceName }
            if (targetVoice != null) {
                tts?.voice = targetVoice
                _selectedVoiceName.value = voiceName
            }
        } catch (_: Exception) {}
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                startVisualizerAnimation()
                onSpeechStarted()
            }

            override fun onDone(utteranceId: String?) {
                stopVisualizerAnimation()
                _isSpeaking.value = false
                _playbackAmplitude.value = 0f
                onSpeechCompleted()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                stopVisualizerAnimation()
                _isSpeaking.value = false
                _playbackAmplitude.value = 0f
                onSpeechError("Vocal synthesis audio stream interrupted.")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                stopVisualizerAnimation()
                _isSpeaking.value = false
                _playbackAmplitude.value = 0f
                onSpeechError("Vocal synthesis audio stream error code: $errorCode")
            }
        })
    }

    private fun startVisualizerAnimation() {
        visualizerJob?.cancel()
        visualizerJob = coroutineScope.launch {
            while (isActive && _isSpeaking.value) {
                // Generate energetic speech audio amplitude oscillation (0.25 .. 0.95)
                val base = Random.nextFloat() * 0.7f + 0.25f
                _playbackAmplitude.value = base
                delay(65)
            }
            _playbackAmplitude.value = 0f
        }
    }

    private fun stopVisualizerAnimation() {
        visualizerJob?.cancel()
        visualizerJob = null
        _playbackAmplitude.value = 0f
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            return
        }

        stop()

        // Clean text for speech (strip markdown characters, code blocks, excessive symbols)
        val sanitized = sanitizeTextForSpeech(text)
        val utteranceId = UUID.randomUUID().toString()

        _isSpeaking.value = true
        startVisualizerAnimation()
        onSpeechStarted()

        tts?.speak(sanitized, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        stopVisualizerAnimation()
        _isSpeaking.value = false
        _playbackAmplitude.value = 0f
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun sanitizeTextForSpeech(input: String): String {
        return input
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted from audio.")
            .replace(Regex("`[^`]*`"), "")
            .replace(Regex("[#*_~>\\[\\]]"), "")
            .replace(Regex("\\n{2,}"), ". ")
            .replace(Regex("\\n"), ", ")
            .trim()
    }
}
