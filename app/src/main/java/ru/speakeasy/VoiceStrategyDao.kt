package ru.speakeasy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceStrategyDao {
    @Query("select * from voice_strategies order by id asc")
    fun observeAllStrategies(): Flow<List<VoiceStrategyEntity>>

    @Query("select * from voice_strategies where id = :id limit 1")
    suspend fun getStrategyById(id: Long): VoiceStrategyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrategy(strategy: VoiceStrategyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(strategies: List<VoiceStrategyEntity>)

    @Update
    suspend fun updateStrategy(strategy: VoiceStrategyEntity)

    @Delete
    suspend fun deleteStrategy(strategy: VoiceStrategyEntity)
}
