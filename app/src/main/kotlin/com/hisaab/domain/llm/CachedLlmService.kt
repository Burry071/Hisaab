package com.hisaab.domain.llm

/**
 * Decorator that wraps any [LlmService] with caching + usage tracking.
 *
 * This is what all agents receive via DI — they never talk to the raw
 * GeminiLlmService / OllamaLlmService directly.
 *
 * Pattern: Decorator / Proxy
 *
 *   CachedLlmService
 *     → checks LlmResponseCache (cache hit → return immediately, no API call)
 *     → on miss: calls inner LlmService.complete()
 *     → records to LlmUsageTracker
 *     → stores result in cache
 *     → returns LlmResponse
 */
class CachedLlmService(
    private val inner        : LlmService,
    private val cache        : LlmResponseCache,
    private val usageTracker : LlmUsageTracker,
) : LlmService {

    override suspend fun complete(
        systemPrompt: String,
        userPrompt: String,
        maxTokens: Int,
    ): LlmResponse {
        val cacheKey = cacheKey(systemPrompt, userPrompt)

        // 1. Cache hit
        cache.get(cacheKey)?.let { cached ->
            usageTracker.record(cached)   // still records cache hit for telemetry
            return cached
        }

        // 2. Cache miss — call real LLM
        val response = inner.complete(systemPrompt, userPrompt, maxTokens)

        // 3. Store and track
        cache.put(cacheKey, response)
        usageTracker.record(response)

        return response
    }

    override suspend fun verify(): VerificationResult = inner.verify()

    private fun cacheKey(system: String, user: String): String {
        val input = "${system.take(50)}::${user.take(100)}"
        return input.replace(" ", "_").replace("\n", "|")
    }
}
