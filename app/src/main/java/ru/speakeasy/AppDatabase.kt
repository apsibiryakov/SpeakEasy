package ru.speakeasy

import android.speech.tts.Voice
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GreetingEntity::class, TalkItemEntity::class, VoiceStrategyEntity::class],
    version = 3,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun greetingDao(): GreetingDao
    abstract fun talkItemDao(): TalkItemDao
    abstract fun voiceStrategyDao(): VoiceStrategyDao
}