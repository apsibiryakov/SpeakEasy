package ru.speakeasy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TalkItemDao {
    // корневые элементы
    @Query("""
        select talk_items.*, (select count(*) from talk_items as children
                                where children.parentId = talk_items.id) as childrenCount
        from talk_items
        where talk_items.parentId is null
        order by talk_items.sortOrder asc, talk_items.id asc
    """)
    fun observeRootItems(): Flow<List<TalkItemEntityWithType>>

    // дочерние элементы для выбранного элемента
    @Query("""
        select talk_items.*, (select count(*) from talk_items as children
                                where children.parentId = talk_items.id) as childrenCount
        from talk_items
        where talk_items.parentId = :itemId
        order by talk_items.sortOrder asc, talk_items.id asc
    """)
    fun observeChildItemsByItem(itemId: Long): Flow<List<TalkItemEntityWithType>>

    // реактивное получение одного элемента
    @Query("""
        select talk_items.* from talk_items where id = :itemId limit 1
    """ )
    fun observeItemById(itemId: Long): Flow<TalkItemEntity?>

    // разовый запрос одного элемента
    @Query("""
        select talk_items.* from talk_items where id = :itemId limit 1
    """ )
    suspend fun getItemById(itemId: Long): TalkItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TalkItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TalkItemEntity>)

    @Update
    suspend fun updateItem(item: TalkItemEntity)

    @Delete
    suspend fun deleteItem(item: TalkItemEntity)


}