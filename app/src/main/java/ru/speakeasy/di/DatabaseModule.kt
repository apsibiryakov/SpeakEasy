package ru.speakeasy.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.speakeasy.data.db.AppDatabase
import ru.speakeasy.data.db.CardActionDao
import ru.speakeasy.data.db.TalkItemDao
import ru.speakeasy.data.db.VoiceStrategyDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "speakeasy_database"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideTalkItemDao(database: AppDatabase): TalkItemDao = database.talkItemDao()

    @Provides
    @Singleton
    fun provideVoiceStrategyDao(database: AppDatabase): VoiceStrategyDao = database.voiceStrategyDao()

    @Provides
    @Singleton
    fun provideCardActionDao(database: AppDatabase): CardActionDao = database.cardActionDao()
}
