package ru.speakeasy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Единое состояние UI для экранов TalkCard Main и TalkCard Simple.
 */
data class TalkUiState(
    val currentCategory: TalkItemEntity? = null,
    val gridItems: List<TalkItemEntityWithType> = emptyList(),
    val focusedCard: CardDetails? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TalkViewModel @Inject constructor(
    private val repository: TalkRepository,
    private val actionExecutor: CardActionExecutor,
    private val audioPlaybackManager: AudioPlaybackManager
) : ViewModel() {

    // ID текущей открытой категории (null = корневой экран)
    private val currentCategoryId = MutableStateFlow<Long?>(null)

    // Детали выбранной карточки для экрана TalkCard Simple (null = экран фокусировки закрыт)
    private val focusedCardDetails = MutableStateFlow<CardDetails?>(null)

    // Реактивно загружаем объект текущей категории для индикатора Current Category
    private val currentCategoryFlow = currentCategoryId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeItem(id)
    }

    // Реактивно загружаем список карточек/категорий для сетки 2x3
    private val gridItemsFlow = currentCategoryId.flatMapLatest { id ->
        repository.observeItems(id)
    }

    // Объединяем все потоки в единый StateFlow для Jetpack Compose
    val uiState: StateFlow<TalkUiState> = combine(
        currentCategoryFlow,
        gridItemsFlow,
        focusedCardDetails
    ) { category, items, focused ->
        TalkUiState(
            currentCategory = category,
            gridItems = items,
            focusedCard = focused,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TalkUiState(isLoading = true)
    )

    /**
     * Обработка клика по элементу в сетке TalkCard Main.
     */
    fun onItemClick(itemWithType: TalkItemEntityWithType) {
        if (itemWithType.isCategory) {
            // Если это категория — открываем её содержимое
            currentCategoryId.value = itemWithType.item.id
        } else {
            // Если это обычная карточка — озвучиваем её и открываем экран TalkCard Simple
            openAndSpeakCard(itemWithType.item.id)
        }
    }

    /**
     * Загружает карточку с её действиями, открывает TalkCard Simple и запускает только озвучку.
     */
    private fun openAndSpeakCard(cardId: Long) {
        viewModelScope.launch {
            val details = repository.getCardDetails(cardId) ?: return@launch
            focusedCardDetails.value = details
            actionExecutor.executeSpeech(details.actions)
        }
    }

    /**
     * Повторное воспроизведение озвучки (кнопка Play на экране TalkCard Simple).
     */
    fun onReplaySpeechClick() {
        val actions = focusedCardDetails.value?.actions ?: return
        viewModelScope.launch {
            actionExecutor.executeSpeech(actions)
        }
    }

    /**
     * Запуск отдельного дополнительного действия по специальной кнопке на экране карточки
     * (например, запуск приложения).
     */
    fun onSingleActionClick(action: CardAction) {
        viewModelScope.launch {
            actionExecutor.executeAction(action)
        }
    }

    /**
     * Запуск всей цепочки действий карточки по порядку sortOrder.
     */
    fun onExecuteAllActionsClick() {
        val actions = focusedCardDetails.value?.actions ?: return
        viewModelScope.launch {
            actionExecutor.executeAll(actions)
        }
    }

    /**
     * Закрытие экрана фокусировки TalkCard Simple (кнопка крестик справа внизу).
     * Останавливает звук и возвращает пользователя в сетку текущей категории.
     */
    fun onCloseFocusedCard() {
        audioPlaybackManager.stop()
        focusedCardDetails.value = null
    }

    /**
     * Возврат на уровень вверх из открытой категории (кнопка "Назад" или клик по Current Category).
     */
    fun onNavigateUp(): Boolean {
        val current = uiState.value.currentCategory ?: return false
        currentCategoryId.value = current.parentId
        return true
    }
}
