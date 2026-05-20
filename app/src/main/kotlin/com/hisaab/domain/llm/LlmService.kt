package com.hisaab.domain.llm

/**
 * Abstract LLM service contract.
 *
 * Every backend (Gemini, OpenAI-compatible, Ollama) implements this.
 * The agent orchestrator calls ONLY this interface — it never knows which
 * provider is active. This is the Strategy pattern.
 */
interface LlmService {

    /**
     * Send a prompt and get a text completion.
     *
     * @param systemPrompt  Persona / constraints for the model
     * @param userPrompt    The actual query (SMS body, pattern description, etc.)
     * @param maxTokens     Response length cap
     * @return              [LlmResponse] with content + token usage metadata
     */
    suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int = 512,
    ): LlmResponse

    /**
     * Lightweight test call to verify API key / endpoint reachability.
     * Returns true if the provider is healthy.
     */
    suspend fun verify(): VerificationResult
}

// ── Response models ───────────────────────────────────────────────────────────

data class LlmResponse(
    val content: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val modelId: String,
    val provider: String,           // "gemini" | "openai" | "ollama"
    val latencyMs: Long,
    val fromCache: Boolean = false,
)

sealed class VerificationResult {
    data class Success(val modelId: String, val latencyMs: Long) : VerificationResult()
    data class Failure(val reason: String, val httpCode: Int? = null) : VerificationResult()

    val isSuccess: Boolean get() = this is Success
}
