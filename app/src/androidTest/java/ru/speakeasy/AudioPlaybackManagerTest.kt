package ru.speakeasy

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.speakeasy.data.db.VoiceEngineType
import ru.speakeasy.data.db.VoiceStrategyEntity
import ru.speakeasy.data.media.AudioPlaybackManager
import ru.speakeasy.data.media.MediaStorageManager
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioPlaybackManagerTest {

    private lateinit var context: Context
    private lateinit var mediaStorage: MediaStorageManager
    private lateinit var testableManager: TestableAudioPlaybackManager
    private lateinit var tempAudioFile: File

    private val ttsStrategy = VoiceStrategyEntity(
        id = 1L,
        name = "Синтезатор речи",
        engineType = VoiceEngineType.TTS,
        isSystem = true
    )

    private val customStrategy = VoiceStrategyEntity(
        id = 2L,
        name = "Записанный голос",
        engineType = VoiceEngineType.CUSTOM_RECORDING,
        isSystem = true
    )

    private val femaleRawStrategy = VoiceStrategyEntity(
        id = 3L,
        name = "Женский голос",
        engineType = VoiceEngineType.BUILT_IN_RAW,
        resourcePrefix = "female_",
        isSystem = true
    )

    private class TestableAudioPlaybackManager(
        context: Context,
        mediaStorage: MediaStorageManager
    ) : AudioPlaybackManager(context, mediaStorage) {
        var lastPlayedFile: File? = null
        var lastPlayedRawResId: Int? = null
        var lastSpokenText: String? = null
        var stopCalledCount = 0

        override fun initTextToSpeech() {
            isTtsReady = true
        }

        override fun playAudioFile(file: File, fallbackText: String) {
            lastPlayedFile = file
        }

        override fun playRawResource(resId: Int, fallbackText: String) {
            lastPlayedRawResId = resId
        }

        override fun speakWithTts(text: String) {
            if (text.isNotBlank()) {
                lastSpokenText = text
            }
        }

        override fun stop() {
            stopCalledCount++
            super.stop()
        }
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mediaStorage = MediaStorageManager(context)
        testableManager = TestableAudioPlaybackManager(context, mediaStorage)
        tempAudioFile = File(context.cacheDir, "test_voice.m4a").apply {
            writeText("dummy audio data")
        }
    }

    @After
    fun tearDown() {
        testableManager.shutdown()
        if (tempAudioFile.exists()) {
            tempAudioFile.delete()
        }
    }

    @Test
    fun playSpeech_usesTts_whenTtsStrategyIsSelected() {
        testableManager.playSpeech(
            speechText = "Я хочу пить",
            audioPath = tempAudioFile.absolutePath,
            strategy = ttsStrategy
        )

        assertNull(testableManager.lastPlayedFile)
        assertEquals("Я хочу пить", testableManager.lastSpokenText)
    }

    @Test
    fun playSpeech_usesAudioFile_whenCustomRecordingStrategyAndFileExists() {
        testableManager.playSpeech(
            speechText = "Позови маму",
            audioPath = tempAudioFile.absolutePath,
            strategy = customStrategy
        )

        assertEquals(tempAudioFile.absolutePath, testableManager.lastPlayedFile?.absolutePath)
        assertNull(testableManager.lastSpokenText)
    }

    @Test
    fun playSpeech_fallsBackToTts_whenCustomFileDoesNotExist() {
        val missingFilePath = File(context.cacheDir, "non_existent_audio.m4a").absolutePath

        testableManager.playSpeech(
            speechText = "Дай яблоко",
            audioPath = missingFilePath,
            strategy = customStrategy
        )

        assertNull(testableManager.lastPlayedFile)
        assertEquals("Дай яблоко", testableManager.lastSpokenText)
    }

    @Test
    fun playSpeech_fallsBackToTts_whenBuiltInRawResourceNotFound() {
        testableManager.playSpeech(
            speechText = "Я хочу суп",
            builtInAudioKey = "missing_sound_key",
            strategy = femaleRawStrategy
        )

        assertNull(testableManager.lastPlayedRawResId)
        assertEquals("Я хочу суп", testableManager.lastSpokenText)
    }

    @Test
    fun playSpeech_stopsPreviousPlaybackBeforeStartingNew() {
        testableManager.playSpeech(speechText = "Один", strategy = ttsStrategy)
        testableManager.playSpeech(speechText = "Два", strategy = ttsStrategy)

        assertTrue(testableManager.stopCalledCount >= 2)
        assertEquals("Два", testableManager.lastSpokenText)
    }
}
