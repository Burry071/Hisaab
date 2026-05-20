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
 * Gemini REST client — uses the `generateContent` endpoint directly via
 * HttpURLConnection (no Google SDK dependency, keeps APK lean).
 *
 * API reference: https://ai.google.dev/api/generate-content
 */
class GeminiLlmService(
    private val config: LlmProvider.GeminiProvider,
) : LlmService {

    override suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int,
    ): LlmResponse = withContext(Dispatchers.IO) {
        val start  = System.currentTimeMillis()
        val url    = buildUrl(config.model.modelId)
        val body   = buildRequestBody(systemPrompt, userPrompt, maxTokens)

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 30_000
            readTimeout    = 60_000
            doOutput = true
            outputStream.write(body.toByteArray())
        }

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val error = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
            throw LlmNetworkException("Gemini API error $responseCode: $error")
        }

        val raw     = connection.inputStream.bufferedReader().readText()
        val latency = System.currentTimeMillis() - start

        parseGeminiResponse(raw, latency)
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
                VerificationResult.Success(modelId = config.model.modelId, latencyMs = latency)
            else
                VerificationResult.Failure("Unexpected response: ${response.content}")
        } catch (e: LlmNetworkException) {
            VerificationResult.Failure(e.message ?: "Network error", e.httpCode)
        } catch (e: Exception) {
            VerificationResult.Failure(e.message ?: "Unknown error")
        }
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private fun buildUrl(modelId: String): String =
        "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent" +
            "?key=${config.resolvedKey}"

    private fun buildRequestBody(system: String, user: String, maxTokens: Int): String {
        val safeUser = user.replace("\"\"\"", "\\\"\\\"\\\"")
        val wrappedUser = "Data Input:\n\"\"\"\n$safeUser\n\"\"\"\n"
        return JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", system) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", wrappedUser) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", maxTokens)
                put("temperature", 0.3)  // low temp for financial reasoning
            })
        }.toString()
    }

    private fun parseGeminiResponse(raw: String, latencyMs: Long): LlmResponse {
        val json      = JSONObject(raw)
        val candidate = json.getJSONArray("candidates").getJSONObject(0)
        val content   = candidate.getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        val usage = json.optJSONObject("usageMetadata")
        return LlmResponse(
            content           = content.trim(),
            promptTokens      = usage?.optInt("promptTokenCount") ?: 0,
            completionTokens  = usage?.optInt("candidatesTokenCount") ?: 0,
            modelId           = config.model.modelId,
            provider          = "gemini",
            latencyMs         = latencyMs,
        )
    }
}
