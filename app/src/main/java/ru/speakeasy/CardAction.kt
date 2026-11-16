package ru.speakeasy

import kotlinx.serialization.Serializable

sealed interface CardAction {
    val id: Long
    val talkItemId: Long
    val sortOrder: Int
}

data class SpeakCardAction(
    override val id: Long = 0,
    override val talkItemId: Long = 0,
    override val sortOrder: Int = 0,
    val speechText: String,
    val voiceStrategyId: Long = 1L,
    val builtInAudioKey: String? = null,
    val audioPath: String? = null
) : CardAction{
    @Serializable
    data class Payload(
        val speechText: String,
        val voiceStrategyId: Long = 1L,
        val builtInAudioKey: String? = null,
        val audioPath: String? = null
    )
}

data class LaunchAppCardAction(
    override val id: Long = 0,
    override val talkItemId: Long = 0,
    override val sortOrder: Int = 0,
    val packageName: String,
    val launchArgs: Map<String, String> = emptyMap()
) : CardAction{
    @Serializable
    data class Payload(
        val packageName: String,
        val launchArgs: Map<String, String> = emptyMap()
    )
}