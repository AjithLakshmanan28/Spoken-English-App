package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SessionRecord::class, SavedPhrase::class],
    version = 1,
    exportSchema = false
)
abstract class SpokenEnglishDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun savedPhraseDao(): SavedPhraseDao

    companion object {
        @Volatile
        private var INSTANCE: SpokenEnglishDatabase? = null

        fun getDatabase(context: Context): SpokenEnglishDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpokenEnglishDatabase::class.java,
                    "spoken_english_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
