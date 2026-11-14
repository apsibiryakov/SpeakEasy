package ru.speakeasy

import android.animation.FloatEvaluator
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class CardWithParent(
    val card: TalkItemEntity,
    val parentCategory: TalkItemEntity?
)

@Singleton
class TalkRepository @Inject constructor(
    private val dao: TalkItemDao,
    private val mediaStorage: MediaStorageManager
) {
    // наблюдает за карточками текущего уровня
    fun observeItems(categoryId: Long?): Flow<List<TalkItemEntityWithType>> {
        return if (categoryId == null) {
            dao.observeRootItems()
        } else {
            dao.observeChildItemsByItem(categoryId)
        }

    }

    // наблюдает за конкретным элементом
    fun observeItem(itemId: Long): Flow<TalkItemEntity?> {
        return dao.observeItemById(itemId)
    }

    // загружает карточку вместе с ее родительской категорией
    suspend fun getCardWithParent(cardId: Long): CardWithParent? {
        val card = dao.getItemById(cardId) ?: return null
        val parentCategory = card.parentId?.let { parentId -> dao.getItemById(parentId) }
        return CardWithParent(card = card, parentCategory = parentCategory)
    }

    suspend fun saveItem(item: TalkItemEntity): Long {
        return dao.insertItem(item)
    }

    suspend fun deleteItem(item: TalkItemEntity){
        mediaStorage.deleteMediaFile(item.audioPath)
        mediaStorage.deleteMediaFile(item.imagePath)
        dao.deleteItem(item)
    }




}