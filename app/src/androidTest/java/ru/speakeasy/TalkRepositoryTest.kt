package ru.speakeasy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    private lateinit var actionDao: CardActionDao
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
        actionDao = database.cardActionDao()
        mediaStorage = MediaStorageManager(context)
        repository = TalkRepository(dao, actionDao, mediaStorage)
    }

    @After
    fun tearDown() {
        database.close()
        File(context.filesDir, "speakeasy_media").deleteRecursively()
    }

    @Test
    fun getCardDetails_returnsCardParentAndPolymorphicActions() = runTest {
        val foodId = repository.saveItem(
            TalkItemEntity(title = "Еда", parentId = null)
        )
        val appleId = repository.saveCardWithActions(
            item = TalkItemEntity(title = "Яблоко", parentId = foodId),
            actions = listOf(
                SpeakCardAction(speechText = "Я хочу яблоко", sortOrder = 1),
                LaunchAppCardAction(
                    packageName = "com.example.game",
                    launchArgs = mapOf("level" to "1"),
                    sortOrder = 2
                )
            )
        )

        val details = repository.getCardDetails(appleId)

        assertNotNull(details)
        assertEquals("Яблоко", details?.card?.title)
        assertEquals("Еда", details?.parentCategory?.title)
        assertEquals(2, details?.actions?.size)
        assertTrue(details?.actions?.get(0) is SpeakCardAction)
        assertTrue(details?.actions?.get(1) is LaunchAppCardAction)
        assertEquals("1", (details?.actions?.get(1) as LaunchAppCardAction).launchArgs["level"])
    }

    @Test
    fun deleteItem_deletesAudioFromSpeakAction_andProtectsExternalFiles() = runTest {
        val internalAudioPath = mediaStorage.createNewAudioFilePath("m4a")
        val internalAudioFile = File(internalAudioPath).apply {
            writeText("fake audio content")
        }
        assertTrue(internalAudioFile.exists())

        val externalFile = File(context.cacheDir, "important_external_file.jpg").apply {
            writeText("external data")
        }
        assertTrue(externalFile.exists())

        val cardId = repository.saveCardWithActions(
            item = TalkItemEntity(
                title = "Тест",
                imagePath = externalFile.absolutePath
            ),
            actions = listOf(
                SpeakCardAction(
                    speechText = "Тест",
                    audioPath = internalAudioPath
                )
            )
        )
        val savedCard = dao.getItemById(cardId)!!

        repository.deleteItem(savedCard)

        assertNull(dao.getItemById(cardId))
        assertTrue(repository.getActionsForItem(cardId).isEmpty())
        assertFalse(internalAudioFile.exists())
        assertTrue(externalFile.exists())

        externalFile.delete()
    }
}
