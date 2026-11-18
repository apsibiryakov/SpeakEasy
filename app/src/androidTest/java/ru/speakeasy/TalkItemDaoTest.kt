package ru.speakeasy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.speakeasy.data.db.AppDatabase
import ru.speakeasy.data.db.TalkItemDao
import ru.speakeasy.data.db.TalkItemEntity

@RunWith(AndroidJUnit4::class)
class TalkItemDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: TalkItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = database.talkItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun itemBecomesCategory_whenChildIsAdded_andRevertsWhenChildDeleted() = runTest {
        val foodId = dao.insertItem(
            TalkItemEntity(title = "Еда", parentId = null)
        )

        var rootItems = dao.observeRootItems().first()
        assertEquals(1, rootItems.size)
        assertEquals(0, rootItems[0].childrenCount)
        assertFalse(rootItems[0].isCategory)

        val appleId = dao.insertItem(
            TalkItemEntity(title = "Яблоко", parentId = foodId)
        )

        rootItems = dao.observeRootItems().first()
        assertEquals(1, rootItems.size)
        assertEquals(1, rootItems[0].childrenCount)
        assertTrue(rootItems[0].isCategory)

        val appleEntity = dao.getItemById(appleId)!!
        dao.deleteItem(appleEntity)

        rootItems = dao.observeRootItems().first()
        assertEquals(1, rootItems.size)
        assertEquals(0, rootItems[0].childrenCount)
        assertFalse(rootItems[0].isCategory)
    }

    @Test
    fun observeChildItemsByItem_returnsOnlyChildrenOfSpecificParent() = runTest {
        val foodId = dao.insertItem(
            TalkItemEntity(title = "Еда", parentId = null, sortOrder = 1)
        )
        dao.insertItem(
            TalkItemEntity(title = "Питье", parentId = null, sortOrder = 2)
        )
        dao.insertAll(
            listOf(
                TalkItemEntity(title = "Суп", parentId = foodId, sortOrder = 1),
                TalkItemEntity(title = "Хлеб", parentId = foodId, sortOrder = 2)
            )
        )

        val foodChildren = dao.observeChildItemsByItem(foodId).first()
        assertEquals(2, foodChildren.size)
        assertEquals("Суп", foodChildren[0].item.title)
        assertEquals("Хлеб", foodChildren[1].item.title)
        assertFalse(foodChildren[0].isCategory)
    }

    @Test
    fun deleteParentCategory_cascadesAndDeletesAllChildren() = runTest {
        val foodId = dao.insertItem(
            TalkItemEntity(title = "Еда", parentId = null)
        )
        val appleId = dao.insertItem(
            TalkItemEntity(title = "Яблоко", parentId = foodId)
        )

        val foodEntity = dao.getItemById(foodId)!!
        dao.deleteItem(foodEntity)

        val deletedChild = dao.getItemById(appleId)
        assertNull(deletedChild)
    }
}
