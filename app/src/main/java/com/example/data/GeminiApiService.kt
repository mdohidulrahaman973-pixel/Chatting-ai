package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val service: GeminiApiService by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateSipraResponse(
        userPrompt: String,
        conversationHistory: List<SipraMessage>,
        systemDirective: String,
        apiKeyOverride: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = if (!apiKeyOverride.isNullOrBlank()) {
            apiKeyOverride.trim()
        } else {
            BuildConfig.GEMINI_API_KEY
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please supply your API key in the HUD Security Override or AI Studio Secrets.")
            )
        }

        try {
            // Build conversation history (up to last 10 turns to conserve tokens)
            val historyContents = mutableListOf<Content>()
            val recentMessages = conversationHistory.takeLast(10)
            for (msg in recentMessages) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                historyContents.add(
                    Content(
                        role = role,
                        parts = listOf(Part(text = msg.text))
                    )
                )
            }
            // Add current user prompt
            historyContents.add(
                Content(
                    role = "user",
                    parts = listOf(Part(text = userPrompt))
                )
            )

            val request = GenerateContentRequest(
                contents = historyContents,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    topP = 0.9f,
                    topK = 40,
                    maxOutputTokens = 1024
                ),
                systemInstruction = Content(
                    parts = listOf(Part(text = systemDirective))
                )
            )

            val response = service.generateContent(apiKey = apiKey, request = request)
            val replyText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull { !it.text.isNullOrBlank() }
                ?.text

            if (!replyText.isNullOrBlank()) {
                Result.success(replyText.trim())
            } else {
                Result.failure(Exception("No response received from SIPRA neural core."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
