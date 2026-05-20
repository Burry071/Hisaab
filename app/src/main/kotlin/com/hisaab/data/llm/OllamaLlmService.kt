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
 * Ollama local model client.
 *
 * Ollama runs on the same device (via Termux on Android) or on the user's
 * home network, so NO data ever leaves the device.
 *
 * Endpoint: POST {baseUrl}/api/chat   (OpenAI-like format in Ollama 0.1.14+)
 *
 * Popular models for Pakistani mid-range phones:
 *   phi3:mini    (3.8B)  — Best quality/size ratio
 *   gemma2:2b    (2B)    — Fastest
 *   llama3.2:3b  (3B)    — Best reasoning
 *   qwen2.5:3b   (3B)    — Good multilingual (some Urdu)
 *
 * Setup instructions shown in SettingsScreen:
 *   1. Install Termux from F-Droid
 *   2. pkg install ollama
 *   3. ollama pull phi3:mini
 *   4. ollama serve   ← runs on 127.0.0.1:11434
 */
class OllamaLlmService(
    private val config: LlmProvider.OllamaProvider,
) : LlmService {

    override suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int,
    ): LlmResponse = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val url   = "${config.baseUrl.trimEnd('/')}/api/chat"
        val body  = buildRequestBody(systemPrompt, userPrompt, maxTokens)

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000   // fast timeout — local connection should be instant
            readTimeout    = 120_000  // models can be slow on low-end hardware
            doOutput = true
            outputStream.write(body.toByteArray())
        }

        val code = connection.responseCode
        if (code != 200) {
            val err = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
            throw LlmNetworkException(
                "Ollama error $code: $err\n" +
                "Is 'ollama serve' running on ${config.baseUrl}?",
                code
            )
        }

        val raw     = connection.inputStream.bufferedReader().readText()
        val latency = System.currentTimeMillis() - start
        parseResponse(raw, latency)
    }

    override suspend fun verify(): VerificationResult = withContext(Dispatchers.IO) {
        // First: check if the model is available
        return@withContext try {
            val tagsUrl = "${config.baseUrl.trimEnd('/')}/api/tags"
            val conn    = (URL(tagsUrl).openConnection() as HttpURLConnection).apply {
                requestMethod  = "GET"
                connectTimeout = 5_000
                readTimeout    = 5_000
            }
            if (conn.responseCode != 200) {
                return@withContext VerificationResult.Failure(
                    "Ollama server not reachable at ${config.baseUrl}"
                )
            }

            val tagsJson = JSONObject(conn.inputStream.bufferedReader().readText())
            val models   = tagsJson.getJSONArray("models")
            val available = (0 until models.length()).map {
                models.getJSONObject(it).getString("name")
            }
            if (available.none { it.startsWith(config.model.substringBefore(":")) }) {
                return@withContext VerificationResult.Failure(
                    "Model '${config.model}' not found. Available: ${available.joinToString()}\n" +
                    "Run: ollama pull ${config.model}"
                )
            }

            val start    = System.currentTimeMillis()
            val response = complete("You are a test.", "Reply with: OK", 10)
            VerificationResult.Success(
                modelId   = config.model,
                latencyMs = System.currentTimeMillis() - start,
            )
        } catch (e: java.net.ConnectException) {
            VerificationResult.Failure(
                "Cannot connect to Ollama at ${config.baseUrl}\n" +
                "Make sure 'ollama serve' is running in Termux."
            )
        } catch (e: Exception) {
            VerificationResult.Failure(e.message ?: "Unknown error")
        }
    }

    private fun buildRequestBody(system: String, user: String, maxTokens: Int): String =
        JSONObject().apply {
            put("model", config.model)
            put("stream", false)
            put("options", JSONObject().apply {
                put("num_predict", maxTokens)
                put("temperature", 0.3)
            })
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", system)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", user)
                })
            })
        }.toString()

    private fun parseResponse(raw: String, latencyMs: Long): LlmResponse {
        val json    = JSONObject(raw)
        val message = json.getJSONObject("message")
        val content = message.getString("content")
        return LlmResponse(
            content          = content.trim(),
            promptTokens     = json.optInt("prompt_eval_count", 0),
            completionTokens = json.optInt("eval_count", 0),
            modelId          = config.model,
            provider         = "ollama",
            latencyMs        = latencyMs,
        )
    }
}

// ── Shared exception ──────────────────────────────────────────────────────────

class LlmNetworkException(
    message: String,
    val httpCode: Int? = null,
) : Exception(message)
