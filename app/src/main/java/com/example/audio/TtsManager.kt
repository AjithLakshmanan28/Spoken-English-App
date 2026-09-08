package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.VoiceSettings
import java.util.Locale

class TtsManager(
    context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) {
    private val TAG = "TtsManager"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentVoiceSettings = VoiceSettings()

    var onSpeakingStarted: (() -> Unit)? = null
    var onSpeakingFinished: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Locale US not supported or missing data")
                }
                isInitialized = true
                applyVoiceSettings(currentVoiceSettings)
                onInitComplete?.invoke(true)
            } else {
                Log.e(TAG, "TextToSpeech init failed with status $status")
                isInitialized = false
                onInitComplete?.invoke(false)
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onSpeakingStarted?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                onSpeakingFinished?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onSpeakingFinished?.invoke()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.w(TAG, "TTS Error: $errorCode")
                onSpeakingFinished?.invoke()
            }
        })
    }

    fun applyVoiceSettings(settings: VoiceSettings) {
        currentVoiceSettings = settings
        if (!isInitialized || tts == null) return

        tts?.setSpeechRate(settings.speechRate)
        tts?.setPitch(settings.pitch)

        val locale = when (settings.accentCode) {
            "en-GB" -> Locale.UK
            "en-AU" -> Locale("en", "AU")
            else -> Locale.US
        }
        try {
            tts?.language = locale
        } catch (e: Exception) {
            Log.e(TAG, "Error setting language: ${e.message}")
        }
    }

    fun speak(text: String, utteranceId: String = System.currentTimeMillis().toString()) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized yet")
            return
        }
        stop()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
        onSpeakingFinished?.invoke()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
        tts = null
        isInitialized = false
    }
}
