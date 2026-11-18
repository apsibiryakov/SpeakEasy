package ru.speakeasy.domain.action

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardActionExecutor @Inject constructor(
    private val hanlers: Map<Class<*>, @JvmSuppressWildcards ActionHandler<*>>
) {
    // конкретное действие
    @Suppress("UNCHECKED_CAST")
    suspend fun executeAction(action: CardAction) {
        val handler = hanlers[action::class.java] as? ActionHandler<CardAction>
        if (handler != null) {
            handler.handle(action)
        } else {
            Log.e(
                "CardActionExecutor",
                "Не зарегистрирован ActionHandler для ${action::class.simpleName}"
            )
        }
    }

    // только озвучка
    suspend fun executeSpeech(actions: List<CardAction>) {
        actions.filterIsInstance<SpeakCardAction>()
            .forEach { action -> executeAction(action) }
    }

    // цепочка действий
    suspend fun executeAll(actions: List<CardAction>) {
        actions.sortedBy { it.sortOrder }
            .forEach { action -> executeAction(action) }
    }
}