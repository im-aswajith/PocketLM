package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    @Query("SELECT * FROM models ORDER BY addedAt DESC")
    fun getAllModels(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE downloadStatus = 'COMPLETED' ORDER BY lastUsedAt DESC")
    fun getDownloadedModels(): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: String): ModelEntity?

    @Query("SELECT * FROM models WHERE id = :id LIMIT 1")
    fun observeModelById(id: String): Flow<ModelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(model: ModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<ModelEntity>)

    @Update
    suspend fun update(model: ModelEntity)

    @Query("UPDATE models SET downloadedBytes = :bytes, downloadSpeed = :speed, etaSeconds = :eta, downloadStatus = :status WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, bytes: Long, speed: String, eta: Long, status: String)

    @Query("UPDATE models SET downloadStatus = :status, localPath = :path WHERE id = :id")
    suspend fun markCompleted(id: String, path: String, status: String = DownloadState.COMPLETED.name)

    @Query("UPDATE models SET lastUsedAt = :time WHERE id = :id")
    suspend fun updateLastUsed(id: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM models WHERE id = :id")
    suspend fun deleteById(id: String)
}
