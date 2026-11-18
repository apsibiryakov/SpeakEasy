# SpeakEasy — Контекст разработки

## О проекте
Android-приложение для альтернативной и дополнительной коммуникации (АДК / AAC) в альбомной ориентации.

## Стек и архитектура
- **Язык / UI:** Kotlin, Jetpack Compose (Material 3), Coil 3
- **База данных:** Room (SQLite, версия 5)
- **Сериализация:** `kotlinx-serialization-json` (полиморфные параметры действий)
- **DI:** Hilt (KSP, мультибиндинг `@IntoMap` для `ActionHandler`)
- **Архитектура:** Clean Architecture + MVVM + Repository + Unidirectional Data Flow (UDF)
- **Корневой пакет:** `ru.speakeasy`

## Структура пакетов
- `ru.speakeasy.data.db` — база данных `AppDatabase` (v5), сущности (`TalkItemEntity`, `VoiceStrategyEntity`, `CardActionEntity`), проекция `TalkItemEntityWithType`, маппер `CardActionMapper`, DAO и инициализатор стартовых данных `DatabaseSeeder`.
- `ru.speakeasy.data.media` — `MediaStorageManager` (изолированная папка `speakeasy_media` и разрешение `res://...`) и `AudioPlaybackManager` (озвучка через `CUSTOM_RECORDING`, `BUILT_IN_RAW` или `TTS`).
- `ru.speakeasy.data.repository` — `TalkRepository` и модель `CardDetails`.
- `ru.speakeasy.domain.action` — полиморфные действия `CardAction` (`SpeakCardAction`, `LaunchAppCardAction`), обработчики `ActionHandler<T>` и диспетчер `CardActionExecutor`.
- `ru.speakeasy.di` — модули Hilt (`DatabaseModule`, `ActionModule`).
- `ru.speakeasy.ui` — `TalkViewModel` (`TalkUiState`), компоненты (`components/TalkCardImage.kt`), экраны (`screens/TalkCardMainScreen.kt`, `screens/TalkCardSimpleScreen.kt`) и тема (`theme/`).

## Текущий статус (Реализовано и покрыто тестами)
- Полный цикл хранения данных, медиафайлов, гибридной озвучки и полиморфных действий карточки (`TalkItemDaoTest`, `TalkRepositoryTest`, `AudioPlaybackManagerTest`, `CardActionExecutorTest`).
- Идемпотентное предзаполнение базы при старте приложения через `DatabaseSeeder` в `SpeakEasyApp`.
- Управление состоянием и навигацией по категориям в `TalkViewModel` (`TalkViewModelTest`).
- Экраны `TalkCardMainScreen` (горизонтальная сетка в 2 строки, зеленая пунктирная рамка для категорий, сплошная для карточек) и `TalkCardSimpleScreen` (фокусировка на карточке, повтор озвучки, кнопки доп. действий).

## План следующих шагов
1. Написать UI-тесты Compose (`TalkScreensTest`) для проверки отрисовки сетки, перехода в категории и открытия экрана фокусировки.
2. Реализовать режим родителя (Parental Mode): создание и редактирование карточек, выбор картинок из галереи, запись голоса с микрофона и настройка действий.
3. Реализовать управление стратегиями озвучки (выбор и создание профилей голоса).
