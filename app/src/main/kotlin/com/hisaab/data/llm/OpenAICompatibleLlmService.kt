package com.hisaab.data.llm

import com.hisaab.domain.llm.LlmProvider
import com.hisaab.domain.llm.LlmResponse
import com.hisaab.domain.llm.LlmService
import com.hisaab.domain.llm.VerificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * OpenAI-compatible client — works with:
 *   • OpenAI   (baseUrl = https://api.openai.com/v1)
 *   • Groq     (baseUrl = https://api.groq.com/openai/v1)
 *   • Together (baseUrl = https://api.together.xyz/v1)
 *   • LM Studio (baseUrl = http://localhost:1234/v1)  ← local desktop bridge
 *
 * Uses the /chat/completions endpoint.
 */
class OpenAICompatibleLlmService(
    private val config: LlmProvider.OpenAICompatibleProvider,
) : LlmService {

    override suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int,
    ): LlmResponse = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val baseUrl = config.baseUrl.trimEnd('/')
        if (baseUrl.startsWith("http://") && !baseUrl.contains("localhost") && !baseUrl.contains("127.0.0.1")) {
            throw IllegalArgumentException("Insecure transport: Remote endpoints must use HTTPS. ($baseUrl)")
        }
        val url   = "$baseUrl/chat/completions"
        val body  = buildRequestBody(systemPrompt, userPrompt, maxTokens)

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            connectTimeout = 30_000
            readTimeout    = 60_000
            doOutput = true
            outputStream.write(body.toByteArray())
        }

        val code = connection.responseCode
        if (code != 200) {
            val err = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            throw LlmNetworkException("OpenAI-compatible error $code: $err", code)
        }

        val raw     = connection.inputStream.bufferedReader().readText()
        val latency = System.currentTimeMillis() - start
        parseResponse(raw, latency)
    }

    override suspend fun verify(): VerificationResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        return@withContext try {
            val response = complete(
                systemPrompt = "You are a test assistant.",
                userPrompt   = "Reply with exactly: OK",
                maxTokens    = 10,
            )
            val latency = System.currentTimeMillis() - start
            if (response.content.contains("OK", ignoreCase = true))
                VerificationResult.Success(modelId = config.model, latencyMs = latency)
            else
                VerificationResult.Failure("Unexpected response: ${response.content}")
        } catch (e: Exception) {
            VerificationResult.Failure(e.message ?: "Connection failed")
        }
    }

    private fun buildRequestBody(system: String, user: String, maxTokens: Int): String {
        val safeUser = user.replace("\"\"\"", "\\\"\\\"\\\"")
        val wrappedUser = "Data Input:\n\"\"\"\n$safeUser\n\"\"\"\n"
        return JSONObject().apply {
            put("model", config.model)
            put("max_tokens", maxTokens)
            put("temperature", 0.3)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", system)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", wrappedUser)
                })
            })
        }.toString()
    }

    private fun parseResponse(raw: String, latencyMs: Long): LlmResponse {
        val json    = JSONObject(raw)
        val choice  = json.getJSONArray("choices").getJSONObject(0)
        val content = choice.getJSONObject("message").getString("content")
        val usage   = json.optJSONObject("usage")
        return LlmResponse(
            content          = content.trim(),
            promptTokens     = usage?.optInt("prompt_tokens") ?: 0,
            completionTokens = usage?.optInt("completion_tokens") ?: 0,
            modelId          = config.model,
            provider         = "openai-compatible",
            latencyMs        = latencyMs,
        )
    }
}
