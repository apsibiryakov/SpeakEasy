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
        voiceStrategyDaoProvider: Provider<VoiceStrategyDao>,
        cardActionDaoProvider: Provider<CardActionDao>
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
                        greetingDaoProvider.get().insertGreeting(
                            GreetingEntity(id = 1, message = "Hello Android from Room")
                        )
                        seedInitialTalkItems(
                            dao = talkItemDaoProvider.get(),
                            strategyDao = voiceStrategyDaoProvider.get(),
                            actionDao = cardActionDaoProvider.get()
                        )
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideGreetingDao(database: AppDatabase): GreetingDao = database.greetingDao()

    @Provides
    @Singleton
    fun provideTalkItemDao(database: AppDatabase): TalkItemDao = database.talkItemDao()

    @Provides
    @Singleton
    fun provideVoiceStrategyDao(database: AppDatabase): VoiceStrategyDao = database.voiceStrategyDao()

    @Provides
    @Singleton
    fun provideCardActionDao(database: AppDatabase): CardActionDao = database.cardActionDao()

    private suspend fun seedInitialTalkItems(
        dao: TalkItemDao,
        strategyDao: VoiceStrategyDao,
        actionDao: CardActionDao
    ) {
        // 1. Создаем системные стратегии озвучки
        val ttsId = strategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 1L,
                name = "Синтезатор речи",
                engineType = VoiceEngineType.TTS,
                isSystem = true
            )
        )
        strategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 2L,
                name = "Записанный голос",
                engineType = VoiceEngineType.CUSTOM_RECORDING,
                isSystem = true
            )
        )
        strategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 3L,
                name = "Женский голос",
                engineType = VoiceEngineType.BUILT_IN_RAW,
                resourcePrefix = "female_",
                isSystem = true
            )
        )

        // 2. Создаем стартовые карточки и привязываем к каждой действие озвучки
        val foodCategoryId = dao.insertItem(
            TalkItemEntity(title = "Еда", parentId = null, sortOrder = 1)
        )
        val drinkId = dao.insertItem(
            TalkItemEntity(title = "Пить", parentId = null, sortOrder = 2)
        )
        val appleId = dao.insertItem(
            TalkItemEntity(title = "Яблоко", parentId = foodCategoryId, sortOrder = 1)
        )
        val soupId = dao.insertItem(
            TalkItemEntity(title = "Суп", parentId = foodCategoryId, sortOrder = 2)
        )

        val defaultSpeakActions = listOf(
            SpeakCardAction(talkItemId = foodCategoryId, speechText = "Я хочу есть", voiceStrategyId = ttsId, sortOrder = 1),
            SpeakCardAction(talkItemId = drinkId, speechText = "Я хочу пить", voiceStrategyId = ttsId, sortOrder = 1),
            SpeakCardAction(talkItemId = appleId, speechText = "Я хочу яблоко", voiceStrategyId = ttsId, sortOrder = 1),
            SpeakCardAction(talkItemId = soupId, speechText = "Я хочу суп", voiceStrategyId = ttsId, sortOrder = 1)
        ).map { CardActionMapper.toEntity(it) }

        actionDao.insertAll(defaultSpeakActions)
    }
}
