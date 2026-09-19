package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadState {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey
    val id: String, // e.g. "Qwen/Qwen2.5-0.5B-Instruct:qwen2.5-0.5b-instruct-q4_k_m.gguf"
    val repoId: String,
    val fileName: String,
    val displayName: String,
    val author: String,
    val parameterSize: String, // "0.5B", "1B", "2B", etc.
    val quantization: String, // "Q4_K_M", "Q5_K_M", "Q8_0", etc.
    val fileSizeBytes: Long,
    val downloadedBytes: Long = 0L,
    val downloadStatus: String = DownloadState.IDLE.name,
    val downloadUrl: String,
    val localPath: String = "",
    val contextLength: Int = 2048,
    val promptTemplate: String = "chatml",
    val systemPrompt: String = "You are a helpful, concise AI assistant running locally on an Android device via Ollama engine.",
    val isFavorite: Boolean = false,
    val downloadSpeed: String = "",
    val etaSeconds: Long = 0L,
    val addedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = 0L
)
