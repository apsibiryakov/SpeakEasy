package ru.speakeasy

import android.net.IpPrefix
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VoiceEngineType{
    TTS, // системный синтез речи speechText
    BUILT_IN_RAW, // встроенные ресурсы из res/raw (префикс + builtInAudioKey)
    CUSTOM_RECORDING // индивидуальная запись голоса в папке speakeasy_media (audioPath)
}

@Entity(tableName = "voice_strategies")
data class VoiceStrategyEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val engineType: VoiceEngineType,
    val resourcePrefix: String = "", // префикс для поиска ресурсов в res/raw (например, "female_" или "male_")
    val isSystem: Boolean = false
)