package com.example.data.model

import java.util.UUID

data class ConversationMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val feedback: String? = null,
    val nativeAlternative: String? = null,
    val pronunciationTip: String? = null,
    val suggestedReplies: List<String> = emptyList(),
    val isTtsPlaying: Boolean = false
)
