package com.example.data.model

data class VoiceSettings(
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val accentCode: String = "en-US", // "en-US", "en-GB", "en-AU"
    val autoPlayAiTts: Boolean = true
)
