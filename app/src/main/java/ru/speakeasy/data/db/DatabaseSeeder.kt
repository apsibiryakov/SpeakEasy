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

        // Вспомогательная функция для создания карточки или категории
        suspend fun createItem(
            title: String,
            speechText: String,
            parentId: Long? = null,
            sortOrder: Int = 0,
            imagePath: String? = null,
            isActive: Boolean = true
        ): Long {
            val itemId = talkItemDao.insertItem(
                TalkItemEntity(
                    title = title,
                    parentId = parentId,
                    sortOrder = sortOrder,
                    imagePath = imagePath,
                    isActive = isActive
                )
            )
            val speakAction = SpeakCardAction(
                talkItemId = itemId,
                speechText = speechText,
                voiceStrategyId = ttsId,
                sortOrder = 1
            )
            cardActionDao.insertAction(CardActionMapper.toEntity(speakAction))
            return itemId
        }

        // --- 2. КОРНЕВОЙ УРОВЕНЬ ---
        // Две быстрые фразы
        createItem(title = "Да", speechText = "Да", sortOrder = 1)
        createItem(title = "Нет", speechText = "Нет", sortOrder = 2)

        // Четыре основные категории
        val wantId = createItem(title = "Хочу", speechText = "Хочу", sortOrder = 3)
        val walkId = createItem(title = "Гулять", speechText = "Гулять", sortOrder = 4)
        val foodDrinkId = createItem(title = "Еда", speechText = "Еда", sortOrder = 5)
        val peopleAnimalsId = createItem(title = "Люди и животные", speechText = "Люди и животные", sortOrder = 6)

        // --- 3. КАТЕГОРИЯ "ХОЧУ" ---
        createItem(title = "Спать", speechText = "Спать", parentId = wantId, sortOrder = 1)
        createItem(title = "Купаться", speechText = "Купаться", parentId = wantId, sortOrder = 2)
        createItem(title = "Туалет", speechText = "Мне нужно в туалет", parentId = wantId, sortOrder = 3)
        val playId = createItem(title = "Играть", speechText = "Играть", parentId = wantId, sortOrder = 4)

        // Подкатегория "Играть" (внутри "Хочу")
        createItem(title = "Прятки", speechText = "Прятки", parentId = playId, sortOrder = 1)
        createItem(title = "Рисовать", speechText = "Рисовать", parentId = playId, sortOrder = 2)
        createItem(title = "Медведь", speechText = "У медведя во бору", parentId = playId, sortOrder = 3)
        createItem(title = "Краски", speechText = "Краски", parentId = playId, sortOrder = 4)

        // --- 4. КАТЕГОРИЯ "ГУЛЯТЬ" ---
        createItem(title = "Игровая комната", speechText = "Игровая", parentId = walkId, sortOrder = 1)
        createItem(title = "Детская площадка", speechText = "Площадка", parentId = walkId, sortOrder = 2)
        createItem(title = "Парк", speechText = "Парк", parentId = walkId, sortOrder = 3)
        createItem(title = "Кафе", speechText = "Кафе", parentId = walkId, sortOrder = 4)
        createItem(title = "Горка", speechText = "Горка", parentId = walkId, sortOrder = 5)

        // --- 5. КАТЕГОРИЯ "ЕДА И ПИТЬЁ" ---
        createItem(title = "Суп", speechText = "Суп", parentId = foodDrinkId, sortOrder = 1)
        createItem(title = "Салат", speechText = "Салат", parentId = foodDrinkId, sortOrder = 2)
        createItem(title = "Вода", speechText = "Вода", parentId = foodDrinkId, sortOrder = 3)
        createItem(title = "Сосиска", speechText = "Сосиска", parentId = foodDrinkId, sortOrder = 4)
        createItem(title = "Хлеб", speechText = "Хлеб", parentId = foodDrinkId, sortOrder = 5)
        createItem(title = "Помидор", speechText = "Помидор", parentId = foodDrinkId, sortOrder = 6)
        createItem(title = "Банан", speechText = "Банан", parentId = foodDrinkId, sortOrder = 7)
        createItem(title = "Сок", speechText = "Сок", parentId = foodDrinkId, sortOrder = 8)

        // --- 6. КАТЕГОРИЯ "ЛЮДИ И ЖИВОТНЫЕ" ---
        // Активные карточки по умолчанию
        createItem(title = "Я", speechText = "Я", parentId = peopleAnimalsId, sortOrder = 1, isActive = true)
        createItem(title = "Мама", speechText = "Мама", parentId = peopleAnimalsId, sortOrder = 2, isActive = true)
        createItem(title = "Папа", speechText = "Папа", parentId = peopleAnimalsId, sortOrder = 3, isActive = true)

        // Дополнительные заготовки (скрыты до включения в режиме редактора)
        createItem(title = "Брат", speechText = "Брат", parentId = peopleAnimalsId, sortOrder = 3, isActive = false)
        createItem(title = "Сестра", speechText = "Сестра", parentId = peopleAnimalsId, sortOrder = 4, isActive = false)
        createItem(title = "Врач", speechText = "Врач", parentId = peopleAnimalsId, sortOrder = 5, isActive = false)
        createItem(title = "Воспитатель", speechText = "Воспитатель", parentId = peopleAnimalsId, sortOrder = 6, isActive = false)
        createItem(title = "Собака", speechText = "Собака", parentId = peopleAnimalsId, sortOrder = 7, isActive = false)
    }
}
