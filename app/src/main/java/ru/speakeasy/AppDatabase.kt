package ru.speakeasy

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GreetingEntity::class,
        TalkItemEntity::class,
        VoiceStrategyEntity::class,
        CardActionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun greetingDao(): GreetingDao
    abstract fun talkItemDao(): TalkItemDao
    abstract fun voiceStrategyDao(): VoiceStrategyDao
    abstract fun cardActionDao(): CardActionDao
}
