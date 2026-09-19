package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun getMessagesList(sessionId: String): List<ChatMessageEntity> = withContext(Dispatchers.IO) {
        chatDao.getMessagesList(sessionId)
    }

    suspend fun getSessionById(sessionId: String): ChatSessionEntity? = withContext(Dispatchers.IO) {
        chatDao.getSessionById(sessionId)
    }

    suspend fun createSession(
        title: String = "New Chat",
        modelId: String,
        modelDisplayName: String,
        systemPrompt: String = "You are a helpful on-device AI assistant.",
        useGpu: Boolean = false,
        threadCount: Int = 4
    ): ChatSessionEntity = withContext(Dispatchers.IO) {
        val session = ChatSessionEntity(
            title = title,
            modelId = modelId,
            modelDisplayName = modelDisplayName,
            systemPrompt = systemPrompt,
            useGpu = useGpu,
            threadCount = threadCount
        )
        chatDao.insertSession(session)
        session
    }

    suspend fun updateSession(session: ChatSessionEntity) = withContext(Dispatchers.IO) {
        chatDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesBySessionId(sessionId)
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun insertMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
        chatDao.touchSession(message.sessionId)
    }

    suspend fun clearSessionMessages(sessionId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesBySessionId(sessionId)
    }
}
