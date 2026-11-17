package ru.speakeasy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TalkViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: TalkRepository
    private lateinit var fakeAudioManager: FakeAudioPlaybackManager
    private lateinit var fakeSpeakHandler: FakeActionHandler<SpeakCardAction>
    private lateinit var fakeLaunchHandler: FakeActionHandler<LaunchAppCardAction>
    private lateinit var viewModel: TalkViewModel

    private class FakeAudioPlaybackManager(
        context: Context,
        mediaStorage: MediaStorageManager
    ) : AudioPlaybackManager(context, mediaStorage) {
        var stopCalledCount = 0

        override fun initTextToSpeech() {
            isTtsReady = true
        }

        override fun stop() {
            stopCalledCount++
        }
    }

    private class FakeActionHandler<T : CardAction> : ActionHandler<T> {
        val executedActions = mutableListOf<T>()
        override suspend fun handle(action: T) {
            executedActions.add(action)
        }
    }

    private suspend fun waitUntil(timeoutMs: Long = 2000L, condition: () -> Boolean) {
        val startTime = System.currentTimeMillis()
        while (!condition()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw AssertionError("Условие не выполнилось за $timeoutMs мс")
            }
            delay(10)
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        val mediaStorage = MediaStorageManager(context)
        repository = TalkRepository(
            dao = database.talkItemDao(),
            actionDao = database.cardActionDao(),
            mediaStorage = mediaStorage
        )

        fakeAudioManager = FakeAudioPlaybackManager(context, mediaStorage)
        fakeSpeakHandler = FakeActionHandler()
        fakeLaunchHandler = FakeActionHandler()

        val handlers: Map<Class<*>, ActionHandler<*>> = mapOf(
            SpeakCardAction::class.java to fakeSpeakHandler,
            LaunchAppCardAction::class.java to fakeLaunchHandler
        )
        val actionExecutor = CardActionExecutor(handlers)

        viewModel = TalkViewModel(
            repository = repository,
            actionExecutor = actionExecutor,
            audioPlaybackManager = fakeAudioManager
        )
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun navigationAndCardFocus_workCorrectly() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect {} }

        // 1. Заполняем тестовые данные: Категория "Еда" -> Карточка "Яблоко"
        val foodId = repository.saveItem(TalkItemEntity(title = "Еда", sortOrder = 1))
        val appleId = repository.saveCardWithActions(
            item = TalkItemEntity(title = "Яблоко", parentId = foodId, sortOrder = 1),
            actions = listOf(
                SpeakCardAction(speechText = "Я хочу яблоко", sortOrder = 1),
                LaunchAppCardAction(packageName = "com.example.game", sortOrder = 2)
            )
        )

        // Ждем, пока корневая категория получит статус isCategory = true
        val rootState = viewModel.uiState.first { state ->
            !state.isLoading && state.gridItems.any { it.isCategory }
        }
        val firstRootItem = rootState.gridItems.first()
        assertNull(rootState.currentCategory)
        assertEquals(1, rootState.gridItems.size)
        assertTrue(firstRootItem.isCategory)

        // 2. Кликаем по категории "Еда" -> проваливаемся внутрь
        viewModel.onItemClick(firstRootItem)

        val categoryState = viewModel.uiState.first { state ->
            state.currentCategory?.id == foodId && state.gridItems.any { it.item.id == appleId }
        }
        val firstChildItem = categoryState.gridItems.first()
        assertEquals("Еда", categoryState.currentCategory?.title)
        assertNull(categoryState.focusedCard)
        assertEquals(1, categoryState.gridItems.size)
        assertEquals("Яблоко", firstChildItem.item.title)
        assertFalse(firstChildItem.isCategory)
        assertEquals(0, fakeSpeakHandler.executedActions.size)

        // 3. Кликаем по карточке "Яблоко" -> открывается TalkCard Simple и срабатывает ТОЛЬКО озвучка
        viewModel.onItemClick(firstChildItem)

        val focusedState = viewModel.uiState.first { it.focusedCard?.card?.id == appleId }
        assertNotNull(focusedState.focusedCard)
        assertEquals("Яблоко", focusedState.focusedCard?.card?.title)
        assertEquals("Еда", focusedState.focusedCard?.parentCategory?.title)

        waitUntil { fakeSpeakHandler.executedActions.size == 1 }
        assertEquals("Я хочу яблоко", fakeSpeakHandler.executedActions.first().speechText)
        assertEquals(0, fakeLaunchHandler.executedActions.size)

        // 4. Повтор озвучки (кнопка Play)
        viewModel.onReplaySpeechClick()
        waitUntil { fakeSpeakHandler.executedActions.size == 2 }
        assertEquals(0, fakeLaunchHandler.executedActions.size)

        // 5. Запуск дополнительного действия по отдельной кнопке
        val launchAction = focusedState.focusedCard!!.actions
            .filterIsInstance<LaunchAppCardAction>()
            .first()
        viewModel.onSingleActionClick(launchAction)
        waitUntil { fakeLaunchHandler.executedActions.size == 1 }

        // 6. Закрытие экрана фокусировки (крестик) -> звук останавливается, остаемся в категории "Еда"
        viewModel.onCloseFocusedCard()
        val afterCloseState = viewModel.uiState.first { it.focusedCard == null }
        assertEquals("Еда", afterCloseState.currentCategory?.title)
        assertTrue(fakeAudioManager.stopCalledCount > 0)

        // 7. Возврат на уровень вверх (в корень)
        val navigatedUp = viewModel.onNavigateUp()
        assertTrue(navigatedUp)
        val backToRootState = viewModel.uiState.first { state ->
            state.currentCategory == null && state.gridItems.any { it.item.id == foodId }
        }
        assertNull(backToRootState.currentCategory)

        collectJob.cancel()
    }
}
