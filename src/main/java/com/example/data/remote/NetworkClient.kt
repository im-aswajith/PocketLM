package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://huggingface.co/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val hfApi: HuggingFaceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(HuggingFaceApi::class.java)
    }

    /**
     * Download a file from Hugging Face with live progress callbacks
     * Supports cancellation and speed calculation
     */
    suspend fun downloadFileWithProgress(
        url: String,
        destinationFile: File,
        authToken: String? = null,
        onProgress: (downloadedBytes: Long, totalBytes: Long, speedBps: Long) -> Unit,
        isCancelled: () -> Boolean
    ): Boolean {
        destinationFile.parentFile?.mkdirs()
        val tempFile = File(destinationFile.absolutePath + ".part")

        val requestBuilder = Request.Builder().url(url)
        if (!authToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $authToken")
        }
        val request = requestBuilder.build()

        var response: Response? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        try {
            response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return false
            }

            val body = response.body ?: return false
            val contentLength = body.contentLength()
            inputStream = body.byteStream()
            outputStream = FileOutputStream(tempFile)

            val buffer = ByteArray(64 * 1024) // 64KB buffer
            var totalRead = 0L
            var lastReportTime = System.currentTimeMillis()
            var bytesSinceLastReport = 0L

            while (true) {
                if (isCancelled()) {
                    tempFile.delete()
                    return false
                }

                val read = inputStream.read(buffer)
                if (read == -1) break

                outputStream.write(buffer, 0, read)
                totalRead += read
                bytesSinceLastReport += read

                val now = System.currentTimeMillis()
                val elapsed = now - lastReportTime
                if (elapsed >= 500) { // report every 500ms
                    val speed = if (elapsed > 0) (bytesSinceLastReport * 1000) / elapsed else 0L
                    onProgress(totalRead, contentLength, speed)
                    lastReportTime = now
                    bytesSinceLastReport = 0L
                }
            }

            outputStream.flush()
            outputStream.close()
            outputStream = null

            // Rename .part to final destination
            if (tempFile.exists()) {
                if (destinationFile.exists()) destinationFile.delete()
                tempFile.renameTo(destinationFile)
            }
            onProgress(totalRead, contentLength, 0L)
            return true
        } catch (e: Exception) {
            tempFile.delete()
            return false
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
            try { response?.close() } catch (_: Exception) {}
        }
    }
}
