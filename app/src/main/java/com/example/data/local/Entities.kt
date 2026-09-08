package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speaking_sessions")
data class SessionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scenarioId: String,
    val scenarioTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val turnCount: Int,
    val fluencyScore: Int,
    val summaryFeedback: String
)

@Entity(tableName = "saved_phrases")
data class SavedPhrase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phrase: String,
    val phonetic: String = "",
    val meaningOrContext: String = "",
    val nativeAlternative: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
)
