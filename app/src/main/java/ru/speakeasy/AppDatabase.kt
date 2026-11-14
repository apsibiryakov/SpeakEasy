package ru.speakeasy

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GreetingEntity::class, TalkItemEntity::class], version = 2, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun greetingDao(): GreetingDao
    abstract fun talkItemDao(): TalkItemDao
}