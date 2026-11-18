package ru.speakeasy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun TalkCardImage(
    imagePath: String?,
    contentDescription: String,
    isCategory: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Преобразуем путь ("res://..." или путь к файлу) в модель, понятную для Coil
    val imageModel = remember(imagePath) {
        when {
            imagePath.isNullOrBlank() -> null
            imagePath.startsWith(MediaStorageManager.RES_PREFIX) -> {
                val resName = imagePath.removePrefix(MediaStorageManager.RES_PREFIX)
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) resId else null
            }
            else -> {
                val file = File(imagePath)
                if (file.exists()) file else null
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Заглушка из базовых иконок Material 3
            Icon(
                imageVector = if (isCategory) Icons.AutoMirrored.Filled.List else Icons.Default.Star,
                contentDescription = contentDescription,
                tint = if (isCategory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
