package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val sender: String, // "USER", "ASSISTANT", "SYSTEM"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokensPerSecond: Float = 0f,
    val tokenCount: Int = 0,
    val timeToFirstTokenMs: Long = 0L,
    val computeDevice: String = "" // "CPU (4 threads)", "GPU (Vulkan fp16)", etc.
)
