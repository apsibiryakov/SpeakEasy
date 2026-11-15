package ru.speakeasy

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        greetingDaoProvider: Provider<GreetingDao>,
        talkItemDaoProvider: Provider<TalkItemDao>,
        voiceStrategyDaoProvider: Provider<VoiceStrategyDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "speakeasy_database"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        // стартовое приветствие
                        greetingDaoProvider.get().insertGreeting(
                            GreetingEntity(id = 1, message = "Hello Android from Room")
                        )
                        // стартовые карточки
                        seedInitialTalkItems(talkItemDaoProvider.get(), voiceStrategyDaoProvider.get())
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideVoiceStrategyDao(database: AppDatabase): VoiceStrategyDao {
        return database.voiceStrategyDao()
    }

    @Provides
    @Singleton
    fun provideGreetingDao(database: AppDatabase): GreetingDao {
        return database.greetingDao()
    }

    @Provides
    @Singleton
    fun provideTalkItemDao(database: AppDatabase): TalkItemDao {
        return database.talkItemDao()
    }

    private suspend fun seedInitialTalkItems(dao: TalkItemDao, strategyDao: VoiceStrategyDao) {
        val ttsId = strategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 1L,
                name = "Синтезатор речи",
                engineType = VoiceEngineType.TTS,
                isSystem = true
            )
        )
        val customId = strategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 2L,
                name = "Записанный голос",
                engineType = VoiceEngineType.CUSTOM_RECORDING,
                isSystem = true
            )
        )
        val femaleId = strategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 3L,
                name = "Женский голос",
                engineType = VoiceEngineType.BUILT_IN_RAW,
                resourcePrefix = "female_",
                isSystem = true
            )
        )
        val foodCategoryId = dao.insertItem(
            TalkItemEntity(
                title = "Еда",
                speechText = "Я хочу есть",
                parentId = null,
                sortOrder = 1,
                voiceStrategyId = ttsId
            )
        )
        dao.insertAll(
            listOf(
                TalkItemEntity(
                    title = "Питье",
                    speechText = "Дай попить",
                    parentId = null,
                    sortOrder = 2,
                    voiceStrategyId = ttsId
                ),
                TalkItemEntity(
                    title = "Яблоко",
                    speechText = "Яблоко",
                    parentId = foodCategoryId,
                    sortOrder = 1,
                    voiceStrategyId = ttsId
                ),
                TalkItemEntity(
                    title = "Суп",
                    speechText = "Суп",
                    parentId = foodCategoryId,
                    sortOrder = 2,
                    voiceStrategyId = ttsId
                )
            )
        )
    }

}