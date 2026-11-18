package ru.speakeasy.data.db

import ru.speakeasy.domain.action.SpeakCardAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val talkItemDao: TalkItemDao,
    private val cardActionDao: CardActionDao,
    private val voiceStrategyDao: VoiceStrategyDao
) {
    suspend fun seedIfEmpty() {
        if (talkItemDao.getRootItemsOnce().isNotEmpty()) return

        // 1. Системные стратегии озвучки
        val ttsId = voiceStrategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 1L,
                name = "Синтезатор речи",
                engineType = VoiceEngineType.TTS,
                isSystem = true
            )
        )
        voiceStrategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 2L,
                name = "Записанный голос",
                engineType = VoiceEngineType.CUSTOM_RECORDING,
                isSystem = true
            )
        )
        voiceStrategyDao.insertStrategy(
            VoiceStrategyEntity(
                id = 3L,
                name = "Женский голос",
                engineType = VoiceEngineType.BUILT_IN_RAW,
                resourcePrefix = "female_",
                isSystem = true
            )
        )

        // 2. Стартовые категории и карточки
        val foodCategoryId = talkItemDao.insertItem(
            TalkItemEntity(title = "Еда", parentId = null, sortOrder = 1)
        )
        val drinkId = talkItemDao.insertItem(
            TalkItemEntity(title = "Пить", parentId = null, sortOrder = 2)
        )
        val appleId = talkItemDao.insertItem(
            TalkItemEntity(title = "Яблоко", parentId = foodCategoryId, sortOrder = 1)
        )
        val soupId = talkItemDao.insertItem(
            TalkItemEntity(title = "Суп", parentId = foodCategoryId, sortOrder = 2)
        )

        // 3. Базовые действия озвучки для стартовых карточек
        val defaultActions = listOf(
            SpeakCardAction(talkItemId = foodCategoryId, speechText = "Я хочу есть", voiceStrategyId = ttsId, sortOrder = 1),
            SpeakCardAction(talkItemId = drinkId, speechText = "Я хочу пить", voiceStrategyId = ttsId, sortOrder = 1),
            SpeakCardAction(talkItemId = appleId, speechText = "Я хочу яблоко", voiceStrategyId = ttsId, sortOrder = 1),
            SpeakCardAction(talkItemId = soupId, speechText = "Я хочу суп", voiceStrategyId = ttsId, sortOrder = 1)
        ).map { CardActionMapper.toEntity(it) }

        cardActionDao.insertAll(defaultActions)
    }
}
