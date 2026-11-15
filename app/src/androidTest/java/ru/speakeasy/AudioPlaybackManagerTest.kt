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
import java.io.File

@RunWith(AndroidJUnit4::class)
class AudioPlaybackManagerTest {
    private lateinit var context: Context
    private lateinit var testableManager: TestableAudioPlaybackManager
    private lateinit var tempAudioFile: File

    // тестовая заглушка вместо реального проигрывания файла
    private class TestableAudioPlaybackManager(context: Context) : AudioPlaybackManager(context) {
        var lastPlayedFile: File? = null
        var lastSpokenText: String? = null
        var stopCalledCount = 0

        override fun initTextToSpeech() {
            isTtsReady = true
        }

        override fun playAudioFile(file: File, fallbackText: String) {
            lastPlayedFile = file
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
        testableManager = TestableAudioPlaybackManager(context)
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
    fun playItem_usesTts_whenAudioPathIsNull() {
        val item = TalkItemEntity(title = "Пить", speechText = "Я хочу пить", audioPath = null)
        testableManager.playItem(item)

        assertNull(testableManager.lastPlayedFile)
        assertEquals("Я хочу пить", testableManager.lastSpokenText)
        assertEquals(1, testableManager.stopCalledCount)
    }

    @Test
    fun playItem_usesAudioFile_whenAudioFileExists() {
        val item = TalkItemEntity(
            title = "Мама",
            speechText = "Позови маму",
            audioPath = tempAudioFile.absolutePath
        )
        testableManager.playItem(item)

        assertEquals(tempAudioFile.absolutePath, testableManager.lastPlayedFile?.absolutePath)
        assertNull(testableManager.lastSpokenText)
    }

    @Test
    fun playItem_fallsBackToTts_whenAudioFileDoesNotExist() {
        val missingFilePath = File(context.cacheDir, "non_existent_audio.m4a").absolutePath
        val item = TalkItemEntity(
            title = "Яблоко",
            speechText = "Дай яблоко",
            audioPath = missingFilePath
        )

        testableManager.playItem(item)

        assertNull(testableManager.lastPlayedFile)
        assertEquals("Дай яблоко", testableManager.lastSpokenText)
    }

    @Test
    fun playItem_stopsPreviousPlaybackBeforeStartingNew() {
        val firstItem = TalkItemEntity(title = "Один", speechText = "Один")
        val secondItem = TalkItemEntity(title = "Два", speechText = "Два")

        testableManager.playItem(firstItem)
        testableManager.playItem(secondItem)

        assertTrue(testableManager.stopCalledCount >= 2)
        assertEquals("Два", testableManager.lastSpokenText)
    }

}