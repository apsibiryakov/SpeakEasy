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
data class TalkItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val parentId: Long? = null,
    val sortOrder: Int = 0,
    val imagePath: String? = null
)
