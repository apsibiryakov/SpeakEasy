package ru.speakeasy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TalkItemEntity::class,
        VoiceStrategyEntity::class,
        CardActionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun talkItemDao(): TalkItemDao
    abstract fun voiceStrategyDao(): VoiceStrategyDao
    abstract fun cardActionDao(): CardActionDao
}