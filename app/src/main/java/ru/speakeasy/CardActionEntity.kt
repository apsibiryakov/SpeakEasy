package ru.speakeasy

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.json.Json

enum class CardActionType {
    SPEAK,
    LAUNCH_APP
}

@Entity(
    tableName = "card_actions",
    foreignKeys = [
        ForeignKey(
            entity = TalkItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["talkItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["talkItemId"])]
)
data class CardActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val talkItemId: Long,
    val actionType: CardActionType,
    val sortOrder: Int = 0,
    val payloadJson: String
)

object CardActionMapper {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun toDomain(entity: CardActionEntity): CardAction {
        return when (entity.actionType) {
            CardActionType.SPEAK -> {
                val payload = json.decodeFromString<SpeakCardAction.Payload>(entity.payloadJson)
                SpeakCardAction(
                    id = entity.id,
                    talkItemId = entity.talkItemId,
                    sortOrder = entity.sortOrder,
                    speechText = payload.speechText,
                    voiceStrategyId = payload.voiceStrategyId,
                    builtInAudioKey = payload.builtInAudioKey,
                    audioPath = payload.audioPath
                )
            }

            CardActionType.LAUNCH_APP -> {
                val payload = json.decodeFromString<LaunchAppCardAction.Payload>(entity.payloadJson)
                LaunchAppCardAction(
                    id = entity.id,
                    talkItemId = entity.talkItemId,
                    sortOrder = entity.sortOrder,
                    packageName = payload.packageName,
                    launchArgs = payload.launchArgs
                )
            }
        }
    }

    fun toEntity(action: CardAction): CardActionEntity {
        return when (action) {
            is SpeakCardAction -> {
                val payload = SpeakCardAction.Payload(
                    speechText = action.speechText,
                    voiceStrategyId = action.voiceStrategyId,
                    builtInAudioKey = action.builtInAudioKey,
                    audioPath = action.audioPath
                )
                CardActionEntity(
                    id = action.id,
                    talkItemId = action.talkItemId,
                    actionType = CardActionType.SPEAK,
                    sortOrder = action.sortOrder,
                    payloadJson = json.encodeToString(payload)
                )
            }

            is LaunchAppCardAction -> {
                val payload = LaunchAppCardAction.Payload(
                    packageName = action.packageName,
                    launchArgs = action.launchArgs
                )
                CardActionEntity(
                    id = action.id,
                    talkItemId = action.talkItemId,
                    actionType = CardActionType.LAUNCH_APP,
                    sortOrder = action.sortOrder,
                    payloadJson = json.encodeToString(payload)
                )
            }
        }
    }
}
