package ru.speakeasy.data.db

import androidx.room.Embedded

data class TalkItemEntityWithType(
    @Embedded
    val item: TalkItemEntity,
    val childrenCount: Int
) {
    val isCategory: Boolean
        get() = childrenCount > 0
}