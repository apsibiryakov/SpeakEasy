package ru.speakeasy.data.media

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.speakeasy.data.db.VoiceEngineType
import ru.speakeasy.data.db.VoiceStrategyEntity
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AudioPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaStorage: MediaStorageManager
) {
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    protected var isTtsReady = false

    // запоминание фразы до инициализации озвучки
    private var pendingSpeechText: String? = null

    init {
        initTextToSpeech()
    }

    protected open fun initTextToSpeech() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val russianLocale = Locale.forLanguageTag("ru-RU")
                val result = tts?.setLanguage(russianLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(
                        "AudioPlaybackManager",
                        "Русский язык для TTS не поддерживается, используем язык по умолчанию"
                    )
                    tts?.language = Locale.getDefault()
                }
                isTtsReady = true

                // озвучка фразы, запомненной до инициализации TTS
                pendingSpeechText?.let { text ->
                    speakWithTts(text)
                    pendingSpeechText = null
                }
            } else {
                Log.e("AudioPlaybackManager", "Ошибка инициализации TextToSpeech: статус $status")
            }
        }
    }

    // Основной метод озвучки: принимает параметры напрямую из SpeakCardAction
    fun playSpeech(
        speechText: String,
        builtInAudioKey: String? = null,
        audioPath: String? = null,
        strategy: VoiceStrategyEntity
    ) {
        stop()

        when (strategy.engineType) {
            VoiceEngineType.CUSTOM_RECORDING -> {
                val audioFile = audioPath?.let { File(it) }
                if (audioFile != null && audioFile.exists() && audioFile.canRead()) {
                    playAudioFile(audioFile, fallbackText = speechText)
                } else {
                    speakWithTts(speechText)
                }
            }

            VoiceEngineType.BUILT_IN_RAW -> {
                if (!builtInAudioKey.isNullOrBlank()) {
                    val resName = "${strategy.resourcePrefix}$builtInAudioKey"
                    val resId = mediaStorage.resolveResourceId("res://$resName", "raw")
                    if (resId != 0) {
                        playRawResource(resId, fallbackText = speechText)
                    } else {
                        speakWithTts(speechText)
                    }
                } else {
                    speakWithTts(speechText)
                }
            }

            VoiceEngineType.TTS -> {
                speakWithTts(speechText)
            }
        }
    }


    protected open fun playRawResource(resId: Int, fallbackText: String) {
        try {
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { releaseMediaPlayer() }
                setOnErrorListener { _, _, _ ->
                    releaseMediaPlayer()
                    speakWithTts(fallbackText)
                    true
                }
                start()
            }

            if (mediaPlayer == null) {
                speakWithTts(fallbackText)
            }
        } catch (e: Exception) {
            Log.e("AudioPlaybackManager", "Ошибка воспроизведения res/raw ($resId): ${e.message}")
            releaseMediaPlayer()
            speakWithTts(fallbackText)
        }
    }

    protected open fun playAudioFile(file: File, fallbackText: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnCompletionListener {
                    releaseMediaPlayer()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(
                        "AudioPlaybackManager",
                        "Ошибка MediaPlayer ($what, $extra), переключаемся на TTS"
                    )
                    releaseMediaPlayer()
                    speakWithTts(fallbackText)
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlaybackManager", "Не удалось запустить аудиофайл: ${e.message}")
            releaseMediaPlayer()
            speakWithTts(fallbackText)
        }
    }

    protected open fun speakWithTts(text: String) {
        if (text.isBlank()) return
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speakeasy_utterance_id")
        } else {
            pendingSpeechText = text
        }
    }

    open fun stop() {
        pendingSpeechText = null
        releaseMediaPlayer()
        if (isTtsReady && tts?.isSpeaking == true) {
            tts?.stop()
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            Log.w("AudioPlaybackManager", "Ошибка при освобождении MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    // полное освобождение ресурсов при завершении работы
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }

}