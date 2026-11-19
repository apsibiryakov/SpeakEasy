package ru.speakeasy.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.speakeasy.data.db.TalkItemEntity
import ru.speakeasy.data.db.TalkItemEntityWithType
import ru.speakeasy.data.repository.CardDetails
import ru.speakeasy.domain.action.CardAction
import ru.speakeasy.domain.action.LaunchAppCardAction
import ru.speakeasy.domain.action.SpeakCardAction
import ru.speakeasy.ui.screens.TalkCardMainScreen
import ru.speakeasy.ui.screens.TalkCardSimpleScreen
import ru.speakeasy.ui.theme.SpeakEasyTheme

@RunWith(AndroidJUnit4::class)
class TalkScreensTest {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun mainScreen_displaysRootItems_andHandlesClicks() {
        val categoryItem = TalkItemEntityWithType(
            item = TalkItemEntity(id = 100L, title = "Test Category", parentId = null),
            childrenCount = 3
        )
        val leafItem = TalkItemEntityWithType(
            item = TalkItemEntity(id = 101L, title = "Test Leaf Card", parentId = null),
            childrenCount = 0
        )
        var clickedItem: TalkItemEntityWithType? = null

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SpeakEasyTheme {
                        TalkCardMainScreen(
                            currentCategory = null,
                            items = listOf(categoryItem, leafItem),
                            onItemClick = { clickedItem = it },
                            onNavigateUpClick = {}
                        )
                    }
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Все карточки").assertIsDisplayed()
            composeTestRule.onNodeWithText("Test Category").assertIsDisplayed()
            composeTestRule.onNodeWithText("Test Leaf Card").assertIsDisplayed()

            composeTestRule.onNodeWithText("Test Leaf Card").performClick()
            assertEquals(leafItem, clickedItem)
        }
    }

    @Test
    fun mainScreen_insideCategory_displaysCategoryHeaderAndBackButton() {
        val parentCategory = TalkItemEntity(id = 200L, title = "Parent Folder", parentId = null)
        val childCard = TalkItemEntityWithType(
            item = TalkItemEntity(id = 201L, title = "Child Item", parentId = 200L),
            childrenCount = 0
        )
        var navigateUpCalled = false

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SpeakEasyTheme {
                        TalkCardMainScreen(
                            currentCategory = parentCategory,
                            items = listOf(childCard),
                            onItemClick = {},
                            onNavigateUpClick = { navigateUpCalled = true }
                        )
                    }
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Parent Folder").assertIsDisplayed()
            composeTestRule.onNodeWithText("Child Item").assertIsDisplayed()

            composeTestRule.onNodeWithContentDescription("Назад").performClick()
            assertTrue(navigateUpCalled)
        }
    }

    @Test
    fun mainScreen_whenEmpty_displaysPlaceholderMessage() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SpeakEasyTheme {
                        TalkCardMainScreen(
                            currentCategory = TalkItemEntity(id = 300L, title = "Empty Folder"),
                            items = emptyList(),
                            onItemClick = {},
                            onNavigateUpClick = {}
                        )
                    }
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("В этой категории пока нет карточек").assertIsDisplayed()
        }
    }

    @Test
    fun simpleScreen_displaysCardDetails_andHandlesControlButtons() {
        val parentCategory = TalkItemEntity(id = 400L, title = "Context Folder")
        val targetCard = TalkItemEntity(id = 401L, title = "Focused Card", parentId = 400L)
        val extraAction = LaunchAppCardAction(
            id = 2L,
            talkItemId = 401L,
            packageName = "com.test.package"
        )
        val details = CardDetails(
            card = targetCard,
            parentCategory = parentCategory,
            actions = listOf(
                SpeakCardAction(id = 1L, talkItemId = 401L, speechText = "Test speech"),
                extraAction
            )
        )

        var replayClicked = false
        var closeClicked = false
        var executedExtraAction: CardAction? = null

        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent {
                    SpeakEasyTheme {
                        TalkCardSimpleScreen(
                            cardDetails = details,
                            onReplayClick = { replayClicked = true },
                            onCloseClick = { closeClicked = true },
                            onSingleActionClick = { executedExtraAction = it }
                        )
                    }
                }
            }
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("Context Folder").assertIsDisplayed()
            composeTestRule.onNodeWithText("Focused Card").assertIsDisplayed()

            composeTestRule.onNodeWithContentDescription("Повторить озвучку").performClick()
            assertTrue(replayClicked)

            composeTestRule.onNodeWithText("Открыть приложение").performClick()
            assertEquals(extraAction, executedExtraAction)

            composeTestRule.onNodeWithContentDescription("Закрыть карточку").performClick()
            assertTrue(closeClicked)
        }
    }
}
