# SpeakEasy — Контекст разработки

## О проекте
Android-приложение для альтернативной и дополнительной коммуникации (АДК / AAC) в альбомной ориентации.

## Стек и архитектура
- **Язык / UI:** Kotlin, Jetpack Compose (Material 3), Coil 3
- **База данных:** Room (SQLite, версия 6)
- **Сериализация:** `kotlinx-serialization-json` (полиморфные параметры действий)
- **DI:** Hilt (KSP, мультибиндинг `@IntoMap` для `ActionHandler`)
- **Архитектура:** Clean Architecture + MVVM + Repository + Unidirectional Data Flow (UDF)
- **Корневой пакет:** `ru.speakeasy`

## Структура пакетов
- `ru.speakeasy.data.db` — база данных `AppDatabase` (v6), сущности (`TalkItemEntity` с полем активности `isActive`, `VoiceStrategyEntity`, `CardActionEntity`), проекция `TalkItemEntityWithType`, маппер `CardActionMapper`, DAO и инициализатор стартовых данных `DatabaseSeeder`.
- `ru.speakeasy.data.media` — `MediaStorageManager` (изолированная папка `speakeasy_media` и разрешение `res://...`) и `AudioPlaybackManager` (озвучка через `CUSTOM_RECORDING`, `BUILT_IN_RAW` или `TTS`).
- `ru.speakeasy.data.repository` — `TalkRepository` и модель `CardDetails`.
- `ru.speakeasy.domain.action` — полиморфные действия `CardAction` (`SpeakCardAction`, `LaunchAppCardAction`), обработчики `ActionHandler<T>` и диспетчер `CardActionExecutor`.
- `ru.speakeasy.di` — модули Hilt (`DatabaseModule`, `ActionModule`).
- `ru.speakeasy.ui` — `TalkViewModel` (`TalkUiState`), компоненты (`components/TalkCardImage.kt`), экраны (`screens/TalkCardMainScreen.kt`, `screens/TalkCardSimpleScreen.kt`) и тема (`theme/`).

## Текущий статус (Реализовано и покрыто тестами)
- **Слой данных и бизнес-логика:** полный цикл работы с карточками, медиафайлами, гибридной озвучкой и полиморфными действиями (`TalkItemDaoTest`, `TalkRepositoryTest`, `AudioPlaybackManagerTest`, `CardActionExecutorTest`). Поддержка скрытия карточек (`isActive = false`) без удаления их ресурсов.
- **Стартовый каталог (`DatabaseSeeder`):** идемпотентное заполнение базы при старте приложения (быстрые фразы «Да», «Нет» и многоуровневые категории «Хочу», «Гулять», «Еда и питьё», «Люди и животные» с активными и скрытыми заготовками).
- **UI и навигация:**
    - `TalkViewModel` (`TalkViewModelTest`).
    - `TalkCardMainScreen`: индикатор `Current Category` с возвратом вверх, горизонтальная сетка в 2 строки, зеленая пунктирная рамка для категорий и сплошная для карточек.
    - `TalkCardSimpleScreen`: экран фокусировки с кнопкой возврата слева вверху, крупной карточкой по центру (клик по ней повторяет озвучку), кнопкой Play справа вверху, кнопками дополнительных действий и закрытием экрана по свайпу влево или вниз.
    - UI-тесты Compose с `HiltTestRunner` (`TalkScreensTest`).

## План следующих шагов
1. Реализовать режим родителя / редактора карточек (Parental Mode):
    - Создание и редактирование карточек и категорий, переключение активности (`isActive`).
    - Выбор картинок из галереи / камеры с копированием в `MediaStorageManager`.
    - Запись голоса с микрофона (`AudioRecorderManager`) для стратегии `CUSTOM_RECORDING`.
    - Настройка действий карточки (`SpeakCardAction`, `LaunchAppCardAction`).
2. Реализовать управление стратегиями озвучки (просмотр и добавление профилей голоса).
