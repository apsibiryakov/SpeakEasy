# SpeakEasy — Контекст разработки

## О проекте
Android-приложение для альтернативной и дополнительной коммуникации (АДК / AAC) в альбомной ориентации.

## Стек и архитектура
- **Язык / UI:** Kotlin, Jetpack Compose (Material 3), Coil 3
- **База данных:** Room (SQLite, версия 4)
- **Сериализация:** `kotlinx-serialization-json` (полиморфные параметры действий)
- **DI:** Hilt (KSP, мультибиндинг `@IntoMap` для `ActionHandler`)
- **Архитектура:** Clean Architecture + MVVM + Repository + Unidirectional Data Flow (UDF)
- **Пакет:** `ru.speakeasy`

## Текущий статус (Реализовано, протестировано и работает вживую)
1. **Слой хранения данных (`Room`):**
   - `TalkItemEntity`: визуал и иерархия (`id`, `title`, `parentId` с `CASCADE`, `sortOrder`, `imagePath`).
   - `TalkItemEntityWithType`: проекция с вычисляемым полем `childrenCount` и свойством `isCategory` (`childrenCount > 0`).
   - `VoiceStrategyEntity` и `VoiceStrategyDao`: таблица `voice_strategies` для профилей озвучки (`TTS`, `BUILT_IN_RAW`, `CUSTOM_RECORDING`) с защитой системных стратегий (`isSystem`) и префиксами ресурсов.
   - `CardActionEntity`, `CardActionDao` и `CardActionMapper`: единая таблица `card_actions` для хранения всех действий карточки (общие колонки + `payloadJson`).
   - `TalkItemDao` (асинхронный `suspend fun getItemById` + реактивные `Flow` запросы) и `AppDatabase` (v4). Покрыто тестами в `TalkItemDaoTest`.
2. **Полиморфные действия и диспетчер (`CardAction`):**
   - Иерархия `sealed interface CardAction`: `SpeakCardAction` (озвучка с привязкой к `voiceStrategyId`) и `LaunchAppCardAction` (запуск приложения с параметрами `launchArgs`).
   - `ActionHandler<T>`, `SpeakActionHandler`, `LaunchAppActionHandler` и `ActionModule` (`@IntoMap` + `@ClassKey`).
   - `CardActionExecutor`: диспетчер с методами `executeSpeech` (вызывается при клике на карточку и по кнопке Play), `executeAction` (запуск отдельного действия) и `executeAll` (запуск всей цепочки по `sortOrder`). Покрыто тестами в `CardActionExecutorTest`.
3. **Управление медиафайлами, Репозиторий и Звук:**
   - `MediaStorageManager`: изолированная папка `context.filesDir/speakeasy_media` и разрешение встроенных ресурсов (`res://...`).
   - `TalkRepository` и `CardDetails`: загрузка карточки с родительской категорией и действиями, транзакционное сохранение (`saveCardWithActions`) и очистка файлов при удалении. Покрыто тестами в `TalkRepositoryTest`.
   - `AudioPlaybackManager`: воспроизведение через `playSpeech` согласно выбранной стратегии с авто-откатом на `TTS`. Покрыто тестами в `AudioPlaybackManagerTest`.
4. **Слой управления состоянием (`TalkViewModel`):**
   - `TalkUiState` и `TalkViewModel`: реактивное объединение потоков (`flatMapLatest`, `combine`, `stateIn`), навигация по дереву категорий, запуск только озвучки при выборе карточки, повтор озвучки, запуск отдельных дополнительных действий и остановка звука при закрытии. Покрыто тестами в `TalkViewModelTest`.
5. **Пользовательский интерфейс (`Jetpack Compose`):**
   - `TalkCardImage`: универсальный загрузчик изображений (поддерживает файлы из `speakeasy_media`, встроенные `res://...` и базовые иконки-заглушки Material 3).
   - `TalkCardMainScreen`: индикатор `Current Category` слева вверху (с возвратом на уровень вверх), горизонтальная сетка карточек в 2 строки (`LazyHorizontalGrid`), категории с зеленой пунктирной рамкой, обычные карточки со сплошной.
   - `TalkCardSimpleScreen`: экран фокусировки с плашкой `Parent Category` слева вверху, крупной карточкой по центру, кнопкой Play справа вверху, кнопками дополнительных действий слева внизу и кнопкой закрытия (крестик) справа внизу.
   - `MainActivity`: корневой контейнер `SpeakEasyAppContent` с обработкой системной кнопки «Назад».

## План следующих шагов
1. Написать UI-тесты Compose (`TalkScreensTest`) для проверки отрисовки сетки, навигации по категориям и работы экрана фокусировки.
2. Реализовать режим родителя (Parental Mode): экран/диалог создания и редактирования карточек, выбор картинок из галереи, запись голоса с микрофона и управление действиями карточки.
3. Реализовать управление стратегиями озвучки (добавление/выбор профилей голоса для карточек).
