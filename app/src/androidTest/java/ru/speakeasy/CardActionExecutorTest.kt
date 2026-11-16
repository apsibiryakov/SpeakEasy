package ru.speakeasy

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardActionExecutorTest {

    private lateinit var fakeSpeakHandler: FakeActionHandler<SpeakCardAction>
    private lateinit var fakeLaunchHandler: FakeActionHandler<LaunchAppCardAction>
    private lateinit var executor: CardActionExecutor

    private class FakeActionHandler<T : CardAction> : ActionHandler<T> {
        val handledActions = mutableListOf<T>()
        override suspend fun handle(action: T) {
            handledActions.add(action)
        }
    }

    @Before
    fun setup() {
        fakeSpeakHandler = FakeActionHandler()
        fakeLaunchHandler = FakeActionHandler()

        val handlersMap: Map<Class<*>, ActionHandler<*>> = mapOf(
            SpeakCardAction::class.java to fakeSpeakHandler,
            LaunchAppCardAction::class.java to fakeLaunchHandler
        )
        executor = CardActionExecutor(handlersMap)
    }

    @Test
    fun executeSpeech_triggersOnlySpeakActions() = runTest {
        val actions = listOf(
            SpeakCardAction(speechText = "Привет", sortOrder = 1),
            LaunchAppCardAction(packageName = "com.example.app", sortOrder = 2)
        )

        executor.executeSpeech(actions)

        assertEquals(1, fakeSpeakHandler.handledActions.size)
        assertEquals("Привет", fakeSpeakHandler.handledActions[0].speechText)
        assertEquals(0, fakeLaunchHandler.handledActions.size)
    }

    @Test
    fun executeAll_triggersAllActionsInSortOrder() = runTest {
        val speak = SpeakCardAction(speechText = "Запускаю", sortOrder = 2)
        val launch = LaunchAppCardAction(packageName = "com.example.app", sortOrder = 1)

        executor.executeAll(listOf(speak, launch))

        assertEquals(1, fakeSpeakHandler.handledActions.size)
        assertEquals(1, fakeLaunchHandler.handledActions.size)
    }
}
