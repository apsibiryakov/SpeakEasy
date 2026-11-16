package ru.speakeasy


import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ActionHandler<in T : CardAction> {
    suspend fun handle(action: T)
}

class SpeakActionHandler @Inject constructor(
    private val audioPlaybackManager: AudioPlaybackManager,
    private val voiceStrategyDao: VoiceStrategyDao
) : ActionHandler<SpeakCardAction> {
    override suspend fun handle(action: SpeakCardAction) {
        val strategy = voiceStrategyDao.getStrategyById(action.voiceStrategyId)
            ?: VoiceStrategyEntity(
                id = 1L,
                name = "Синтезатор речи",
                engineType = VoiceEngineType.TTS,
                isSystem = true
            )
        audioPlaybackManager.playSpeech(
            speechText = action.speechText,
            builtInAudioKey = action.builtInAudioKey,
            audioPath = action.audioPath,
            strategy = strategy
        )
    }
}

class LaunchAppActionHandler @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionHandler<LaunchAppCardAction> {

    override suspend fun handle(action: LaunchAppCardAction) {
        if (action.packageName.isBlank()) return

        try {
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(action.packageName)
                ?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    action.launchArgs.forEach { (key, value) ->
                        putExtra(key, value)
                    }
                }

            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Log.w("LaunchAppActionHandler", "Приложение ${action.packageName} не найдено на устройстве")
            }
        } catch (e: Exception) {
            Log.e("LaunchAppActionHandler", "Ошибка запуска ${action.packageName}: ${e.message}")
        }
    }
}