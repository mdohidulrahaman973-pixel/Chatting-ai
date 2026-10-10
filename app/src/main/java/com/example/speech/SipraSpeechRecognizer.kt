package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SipraSpeechRecognizer(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningStarted: () -> Unit,
    private val onEndOfSpeech: () -> Unit,
    private val onListeningCancelled: () -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _partialText.value = ""
            onListeningStarted()
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Normalize rmsdB (-2 to ~10 dB typically) to 0.0 .. 1.0 range
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioAmplitude.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
            _audioAmplitude.value = 0f
            onEndOfSpeech()
        }

        override fun onError(error: Int) {
            _isListening.value = false
            _audioAmplitude.value = 0f
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording protocol failure."
                SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Acoustic sensor permission required."
                SpeechRecognizer.ERROR_NETWORK -> "Network protocol failure for speech recognition."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech network telemetry timed out."
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech input detected."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice subsystem busy."
                SpeechRecognizer.ERROR_SERVER -> "Recognition server encounter failure."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech capture timed out."
                else -> "Acoustic sensor error code: $error"
            }
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onListeningCancelled()
            } else {
                onError(errorMessage)
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            _audioAmplitude.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                _partialText.value = ""
                onSpeechRecognized(recognizedText.trim())
            } else {
                onListeningCancelled()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                _partialText.value = partial
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition subsystem unavailable on this device.")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
            onListeningStarted()
        } catch (e: Exception) {
            _isListening.value = false
            onListeningCancelled()
            onError("Failed to initialize acoustic sensor: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
        _audioAmplitude.value = 0f
    }

    fun destroy() {
        stopListening()
    }
}
