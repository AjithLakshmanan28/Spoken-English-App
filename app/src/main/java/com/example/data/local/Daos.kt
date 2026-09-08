package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM speaking_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionRecord): Long

    @Query("SELECT COUNT(*) FROM speaking_sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT AVG(fluencyScore) FROM speaking_sessions")
    fun getAverageScore(): Flow<Double?>
}

@Dao
interface SavedPhraseDao {
    @Query("SELECT * FROM saved_phrases ORDER BY addedTimestamp DESC")
    fun getAllSavedPhrases(): Flow<List<SavedPhrase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: SavedPhrase): Long

    @Query("DELETE FROM saved_phrases WHERE id = :id")
    suspend fun deletePhrase(id: Long)

    @Query("SELECT COUNT(*) FROM saved_phrases")
    fun getCount(): Flow<Int>
}
