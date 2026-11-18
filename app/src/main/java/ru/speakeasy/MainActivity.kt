package ru.speakeasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.speakeasy.ui.TalkViewModel
import ru.speakeasy.ui.screens.TalkCardMainScreen
import ru.speakeasy.ui.screens.TalkCardSimpleScreen
import ru.speakeasy.ui.theme.SpeakEasyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            SpeakEasyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpeakEasyAppContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }

    }
}

@Composable
fun SpeakEasyAppContent(
    modifier: Modifier = Modifier,
    viewModel: TalkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = uiState.focusedCard != null || uiState.currentCategory != null) {
        if (uiState.focusedCard != null) {
            viewModel.onCloseFocusedCard()
        } else {
            viewModel.onNavigateUp()
        }
    }

    val focusedCard = uiState.focusedCard
    if (focusedCard != null) {
        TalkCardSimpleScreen(
            cardDetails = focusedCard,
            onReplayClick = viewModel::onReplaySpeechClick,
            onCloseClick = viewModel::onCloseFocusedCard,
            onSingleActionClick = viewModel::onSingleActionClick,
            modifier = modifier
        )
    } else {
        TalkCardMainScreen(
            currentCategory = uiState.currentCategory,
            items = uiState.gridItems,
            onItemClick = viewModel::onItemClick,
            onNavigateUpClick = viewModel::onNavigateUp,
            modifier = modifier
        )
    }

}
