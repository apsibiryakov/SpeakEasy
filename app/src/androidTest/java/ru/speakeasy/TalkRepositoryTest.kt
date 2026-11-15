package ru.speakeasy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class TalkRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var dao: TalkItemDao
    private lateinit var strategyDao: VoiceStrategyDao
    private lateinit var mediaStorage: MediaStorageManager
    private lateinit var repository: TalkRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.talkItemDao()
        strategyDao = database.voiceStrategyDao()
        mediaStorage = MediaStorageManager(context)
        repository = TalkRepository(dao, mediaStorage)

        // Вставляем базовую стратегию для соблюдения целостности ForeignKey
        runBlocking {
            strategyDao.insertStrategy(
                VoiceStrategyEntity(
                    id = 1L,
                    name = "Синтезатор речи",
                    engineType = VoiceEngineType.TTS,
                    isSystem = true
                )
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
        File(context.filesDir, "speakeasy_media").deleteRecursively()
    }

    @Test
    fun getCardWithParent_returnsCardAndItsParentCategory() = runTest {
        val foodId = repository.saveItem(
            TalkItemEntity(title = "Еда", speechText = "Еда", parentId = null)
        )
        val appleId = repository.saveItem(
            TalkItemEntity(title = "Яблоко", speechText = "Я хочу яблоко", parentId = foodId)
        )

        val result = repository.getCardWithParent(appleId)

        assertNotNull(result)
        assertEquals("Яблоко", result?.card?.title)
        assertNotNull(result?.parentCategory)
        assertEquals("Еда", result?.parentCategory?.title)
    }

    @Test
    fun getCardWithParent_returnsNullParent_whenCardIsAtRoot() = runTest {
        val drinkId = repository.saveItem(
            TalkItemEntity(title = "Пить", speechText = "Я хочу пить", parentId = null)
        )

        val result = repository.getCardWithParent(drinkId)

        assertNotNull(result)
        assertEquals("Пить", result?.card?.title)
        assertNull(result?.parentCategory)
    }

    @Test
    fun deleteItem_deletesFilesInsideMediaFolder_andProtectsExternalFiles() = runTest {
        val internalAudioPath = mediaStorage.createNewAudioFilePath("m4a")
        val internalAudioFile = File(internalAudioPath).apply {
            writeText("fake audio content")
        }
        assertTrue(internalAudioFile.exists())

        val externalFile = File(context.cacheDir, "important_external_file.jpg").apply {
            writeText("external data")
        }
        assertTrue(externalFile.exists())

        val cardId = repository.saveItem(
            TalkItemEntity(
                title = "Тест",
                speechText = "Тест",
                audioPath = internalAudioPath,
                imagePath = externalFile.absolutePath
            )
        )
        val savedCard = dao.getItemById(cardId)!!

        repository.deleteItem(savedCard)

        assertNull(dao.getItemById(cardId))
        assertFalse(internalAudioFile.exists())
        assertTrue(externalFile.exists())

        externalFile.delete()
    }
}
