package ru.speakeasy.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.speakeasy.data.db.TalkItemEntityWithType
import ru.speakeasy.data.db.TalkItemEntity
import ru.speakeasy.ui.components.TalkCardImage

// Зеленый цвет для пунктирной рамки категорий
private val CategoryGreenBorder = Color(0xFF2E7D32)

@Composable
fun TalkCardMainScreen(
    currentCategory: TalkItemEntity?,
    items: List<TalkItemEntityWithType>,
    onItemClick: (TalkItemEntityWithType) -> Unit,
    onNavigateUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Верхняя панель: индикатор Current Category слева вверху
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CurrentCategoryBadge(
                category = currentCategory,
                onClick = onNavigateUpClick
            )
        }

        // Основная область: горизонтальная сетка в 2 строки
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "В этой категории пока нет карточек",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = items,
                    key = { it.item.id }
                ) { itemWithType ->
                    TalkGridCard(
                        itemWithType = itemWithType,
                        onClick = { onItemClick(itemWithType) },
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1.1f) // Удобная пропорция карточки в альбомном режиме
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentCategoryBadge(
    category: TalkItemEntity?,
    onClick: () -> Unit
) {
    val isRoot = category == null
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isRoot) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            CategoryGreenBorder.copy(alpha = 0.15f)
        },
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isRoot, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRoot) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = CategoryGreenBorder,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = category?.title ?: "Все карточки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRoot) MaterialTheme.colorScheme.onSurfaceVariant else CategoryGreenBorder
            )
        }
    }
}

@Composable
private fun TalkGridCard(
    itemWithType: TalkItemEntityWithType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCategory = itemWithType.isCategory
    val borderColor = if (isCategory) CategoryGreenBorder else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        // Рисуем пунктирную зеленую рамку для категорий и сплошную для карточек
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val pathEffect = if (isCategory) {
                PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 8.dp.toPx()), 0f)
            } else {
                null
            }
            drawRoundRect(
                color = borderColor,
                style = Stroke(width = strokeWidth, pathEffect = pathEffect),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TalkCardImage(
                imagePath = itemWithType.item.imagePath,
                contentDescription = itemWithType.item.title,
                isCategory = isCategory,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = itemWithType.item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
