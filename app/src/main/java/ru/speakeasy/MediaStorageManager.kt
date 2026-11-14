package ru.speakeasy

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.cert.Extension
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // общая папка с медиа
    private val mediaDir: File by lazy {
        File(context.filesDir, "speakeasy_media").apply {
            if (!exists()) {
                mkdirs()
            }

        }
    }

    // новый пустой файл для аудио
    fun createNewAudioFilePath(extension: String = "m4a"): String {
        val fileName = "audio_${UUID.randomUUID()}.$extension"
        return File(mediaDir, fileName).absolutePath
    }

    // копирует файл в папку с медиа
    suspend fun copyUriToMediaFolder(
        sourceUri: Uri,
        prefix: String = "media",
        extension: String = "dat"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "${prefix}_${UUID.randomUUID()}.$extension"
            val destinationFile = File(mediaDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return@withContext null

            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    // удаляет файл из папки медиа
    fun deleteMediaFile(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        val file = File(filePath)
        if (file.exists() && file.parentFile?.canonicalPath == mediaDir.canonicalPath) {
            file.delete()
        }
    }

}