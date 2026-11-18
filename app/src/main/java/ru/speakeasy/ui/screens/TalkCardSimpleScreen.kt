package ru.speakeasy.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.speakeasy.data.repository.CardDetails
import ru.speakeasy.domain.action.CardAction
import ru.speakeasy.domain.action.LaunchAppCardAction
import ru.speakeasy.domain.action.SpeakCardAction
import ru.speakeasy.ui.components.TalkCardImage

@Composable
fun TalkCardSimpleScreen(
    cardDetails: CardDetails,
    onReplayClick: () -> Unit,
    onCloseClick: () -> Unit,
    onSingleActionClick: (CardAction) -> Unit,
    modifier: Modifier = Modifier
) {
// Фильтруем дополнительные действия (всё, кроме базовой озвучки)
    val extraActions = cardDetails.actions.filter { it !is SpeakCardAction }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {

// 1. Слева вверху: Parent Category
        Surface(
            shape = RoundedCornerShape(12.dp),

            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.align(Alignment.TopStart)
        ) {

            Text(
                text = cardDetails.parentCategory?.title ?: "Все карточки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

// 2. Справа вверху: Кнопка Play (повтор озвучки)
        FilledIconButton(
            onClick = onReplayClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),

            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
        ) {

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Повторить озвучку",
                modifier = Modifier.size(36.dp)
            )
        }

// 3. По центру: Крупная выбранная карточка
        Surface(
            shape = RoundedCornerShape(24.dp),

            color = MaterialTheme.colorScheme.surface,
            border =

                BorderStroke(3.dp, MaterialTheme.colorScheme.primary),

            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.75f)
                .aspectRatio(1.1f)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                TalkCardImage(
                    imagePath = cardDetails.card.imagePath,
                    contentDescription = cardDetails.card.title,
                    isCategory = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = cardDetails.card.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 4. Слева внизу: Кнопки дополнительных действий (если они есть у карточки)
        if (extraActions.isNotEmpty()) {

            Row(
                modifier = Modifier.align(Alignment.BottomStart),

                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                extraActions.forEach { action ->
                    val buttonLabel = when (action) {
                        is LaunchAppCardAction -> "Открыть приложение"
                        else -> "Выполнить действие"
                    }
                    Button(
                        onClick = { onSingleActionClick(action) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonLabel,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

// 5. Справа внизу: Кнопка закрытия (крестик)
        FilledIconButton(
            onClick = onCloseClick,
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),

            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(64.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрыть карточку",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
