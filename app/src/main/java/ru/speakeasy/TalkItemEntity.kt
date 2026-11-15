package ru.speakeasy

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "talk_items", foreignKeys = [ForeignKey(
        entity = TalkItemEntity::class,
        parentColumns = ["id"],
        childColumns = ["parentId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = VoiceStrategyEntity::class,
        parentColumns = ["id"],
        childColumns = ["voiceStrategyId"],
        onDelete = ForeignKey.RESTRICT
    )], indices = [Index(value = ["parentId"]), Index(value = ["voiceStrategyId"])]

)
data class TalkItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // текст на карточке
    val title: String,
    // ид родительской категории
    val parentId: Long? = null,
    // индекс в списке
    val sortOrder: Int = 0,
    // фраза для озвучки через TTS, используется, если нет пользовательской аудиозаписи
    val speechText: String = title,
    // стратегия озвучки для карточки
    val voiceStrategyId: Long = 1L,
    // базовый ключ звука (например, для базового ключа apple путь с учетом префикса стратегии может быть res/raw/female_apple)
    val builtInAudioKey: String? = null,
    // путь или URI записанной озвучки, если null, то воспроизводится speechText через TTS
    val audioPath: String? = null,
    // путь или URI пользовательской картинки (для встроенной res://...)
    val imagePath: String? = null
)