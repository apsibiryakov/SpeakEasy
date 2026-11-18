package ru.speakeasy.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.speakeasy.domain.action.CardAction
import ru.speakeasy.domain.action.LaunchAppCardAction
import ru.speakeasy.domain.action.SpeakCardAction
import ru.speakeasy.data.db.TalkItemEntityWithType
import ru.speakeasy.data.db.CardActionDao
import ru.speakeasy.data.db.CardActionMapper
import ru.speakeasy.data.db.TalkItemDao
import ru.speakeasy.data.db.TalkItemEntity
import ru.speakeasy.data.db.VoiceEngineType
import ru.speakeasy.data.db.VoiceStrategyDao
import ru.speakeasy.data.db.VoiceStrategyEntity
import ru.speakeasy.data.media.MediaStorageManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Полная модель карточки для экрана TalkCard Simple:
 * сама карточка, её родительская категория и список полиморфных действий.
 */
data class CardDetails(
    val card: TalkItemEntity,
    val parentCategory: TalkItemEntity?,
    val actions: List<CardAction>
)

@Singleton
class TalkRepository @Inject constructor(
    private val dao: TalkItemDao,
    private val actionDao: CardActionDao,
    private val mediaStorage: MediaStorageManager
) {

    fun observeItems(categoryId: Long?): Flow<List<TalkItemEntityWithType>> {
        return if (categoryId == null) {
            dao.observeRootItems()
        } else {
            dao.observeChildItemsByItem(categoryId)
        }
    }

    fun observeItem(itemId: Long): Flow<TalkItemEntity?> {
        return dao.observeItemById(itemId)
    }

    /**
     * Наблюдает за действиями карточки, автоматически преобразуя их из JSON в доменные модели.
     */
    fun observeActionsForItem(itemId: Long): Flow<List<CardAction>> {
        return actionDao.observeActionsForItem(itemId).map { entities ->
            entities.map { CardActionMapper.toDomain(it) }
        }
    }

    /**
     * Загружает все действия карточки разово (например, при нажатии на карточку на главном экране).
     */
    suspend fun getActionsForItem(itemId: Long): List<CardAction> {
        return actionDao.getActionsForItem(itemId).map { CardActionMapper.toDomain(it) }
    }

    /**
     * Загружает карточку, её родительскую категорию и список действий для экрана TalkCard Simple.
     */
    suspend fun getCardDetails(cardId: Long): CardDetails? {
        val card = dao.getItemById(cardId) ?: return null
        val parentCategory = card.parentId?.let { parentId ->
            dao.getItemById(parentId)
        }
        val actions = getActionsForItem(cardId)

        return CardDetails(
            card = card,
            parentCategory = parentCategory,
            actions = actions
        )
    }

    /**
     * Сохраняет или обновляет карточку.
     */
    suspend fun saveItem(item: TalkItemEntity): Long {
        return dao.insertItem(item)
    }

    /**
     * Сохраняет карточку вместе со списком её действий.
     */
    suspend fun saveCardWithActions(item: TalkItemEntity, actions: List<CardAction>): Long {
        val savedCardId = dao.insertItem(item)
        val entities = actions.map { action ->
            val updatedAction = when (action) {
                is SpeakCardAction -> action.copy(talkItemId = savedCardId)
                is LaunchAppCardAction -> action.copy(talkItemId = savedCardId)
            }
            CardActionMapper.toEntity(updatedAction)
        }
        actionDao.insertAll(entities)
        return savedCardId
    }

    /**
     * Удаляет карточку и все связанные с ней медиафайлы (картинку карточки и аудиофайлы из действий).
     */
    suspend fun deleteItem(item: TalkItemEntity) {
        // 1. Удаляем аудиофайлы из действий озвучки
        val actions = getActionsForItem(item.id)
        actions.filterIsInstance<SpeakCardAction>().forEach { speakAction ->
            mediaStorage.deleteMediaFile(speakAction.audioPath)
        }

        // 2. Удаляем картинку самой карточки
        mediaStorage.deleteMediaFile(item.imagePath)

        // 3. Удаляем карточку из БД (записи в card_actions удалятся каскадно через ForeignKey)
        dao.deleteItem(item)
    }

}
