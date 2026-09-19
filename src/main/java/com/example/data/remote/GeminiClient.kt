package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

object GeminiClient {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val requestAdapter by lazy { moshi.adapter(GeminiGenerateRequest::class.java) }
    private val responseAdapter by lazy { moshi.adapter(GeminiGenerateResponse::class.java) }

    suspend fun generate(
        modelName: String,
        prompt: String,
        history: List<Pair<String, String>>,
        systemPrompt: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val modelsToTry = listOf(
            "gemini-3.1-flash-lite-preview",
            "gemini-flash-latest",
            "gemini-3.5-flash"
        )

        val contentsList = mutableListOf<GeminiContent>()
        // Pass up to 6 recent history turns for conversational context
        val recentHistory = history.takeLast(6)
        for ((sender, text) in recentHistory) {
            val role = if (sender.equals("USER", ignoreCase = true)) "user" else "model"
            contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = text)), role = role))
        }
        contentsList.add(GeminiContent(parts = listOf(GeminiPart(text = prompt)), role = "user"))

        val personaPrompt = buildString {
            append("You are $modelName, an intelligent on-device AI model running in PocketLM. ")
            if (systemPrompt.isNotBlank()) {
                append("System instruction: $systemPrompt. ")
            }
            append("Answer the user's prompt directly, comprehensively, and practically. Use clear Markdown with headings, bullet points, code blocks, or step-by-step instructions where appropriate.")
        }

        val requestPayload = GeminiGenerateRequest(
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = personaPrompt))),
            contents = contentsList
        )

        val jsonString = try {
            requestAdapter.toJson(requestPayload)
        } catch (_: Throwable) {
            return@withContext null
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(mediaType)

        for (geminiModel in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$apiKey"
                val httpRequest = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(httpRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val parsed = responseAdapter.fromJson(bodyString)
                            val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            if (!text.isNullOrBlank()) {
                                return@withContext text.trim()
                            }
                        }
                    }
                }
            } catch (_: Throwable) {
                // If this model times out or encounters 503, try next model
            }
        }
        null
    }
}
