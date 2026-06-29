package ru.speakeasy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GreetingViewModel(private val greetingDao: GreetingDao) : ViewModel() {
    // Считываем реактивный поток данных из Room
    val uiState: StateFlow<String> = greetingDao.getGreeting()
        .map { entity -> entity?.message ?: "Загрузка..." }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Загрузка"
        )

    // Метод обновления текста в БД через Корутины
    fun updateGreetingText(newText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            greetingDao.updateGreeting(GreetingEntity(id = 1, message = newText))

        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context =
                    this[AndroidViewModelFactory.APPLICATION_KEY] as SpeakEasyApp
                GreetingViewModel(context.database.greetingDao())
            }
        }
    }
}