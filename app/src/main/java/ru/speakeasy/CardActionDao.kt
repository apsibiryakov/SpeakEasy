package ru.speakeasy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardActionDao {

    @Query("SELECT * FROM card_actions WHERE talkItemId = :itemId ORDER BY sortOrder ASC, id ASC")
    fun observeActionsForItem(itemId: Long): Flow<List<CardActionEntity>>

    @Query("SELECT * FROM card_actions WHERE talkItemId = :itemId ORDER BY sortOrder ASC, id ASC")
    suspend fun getActionsForItem(itemId: Long): List<CardActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: CardActionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actions: List<CardActionEntity>)

    @Update
    suspend fun updateAction(action: CardActionEntity)

    @Delete
    suspend fun deleteAction(action: CardActionEntity)
}
