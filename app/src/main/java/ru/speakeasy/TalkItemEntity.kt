package ru.speakeasy
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "talk_items",
    foreignKeys = [
        ForeignKey(
            entity = TalkItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentId"])]

)
data class TalkItemEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // текст на карточке
    val title: String,
    // ид родительской категории
    val parentId: Long? = null,
    // индекс в списке
    val sortOrder: Int = 0,
    // фраза для озвучки через TTS, используется, если нет пользовательской аудиозаписи
    val speechText: String = title,
    // путь или URI записанной озвучки, если null, то воспроизводится speechText через TTS
    val audioPath: String? = null,
    // путь или URI пользовательской картинки
    val imagePath: String? = null
)