package com.hisaab.domain.llm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Tracks LLM token usage per session and cumulatively.
 *
 * Exposed as StateFlow so the Settings screen can show a live
 * "You've used ~X tokens today (~PKR Y.YY estimated cost)" badge.
 *
 * Pricing reference (approximate, as of mid-2025):
 *   Gemini Flash:  $0.075 / 1M input tokens, $0.30 / 1M output tokens
 *   GPT-4o-mini:   $0.15  / 1M input tokens, $0.60 / 1M output tokens
 *   Groq Llama3:   Free tier (generous)
 *   Ollama local:  $0.00 (compute cost not tracked)
 */
class LlmUsageTracker {

    private val _sessionStats = MutableStateFlow(UsageStats())
    val sessionStats: kotlinx.coroutines.flow.StateFlow<UsageStats> = _sessionStats.asStateFlow()

    /** Call after every LLM completion to accumulate usage. */
    fun record(response: LlmResponse) {
        if (response.fromCache) return  // don't count cache hits

        val current = _sessionStats.value
        _sessionStats.value = current.copy(
            totalCalls       = current.totalCalls + 1,
            totalInputTokens = current.totalInputTokens + response.promptTokens,
            totalOutputTokens= current.totalOutputTokens + response.completionTokens,
            totalLatencyMs   = current.totalLatencyMs + response.latencyMs,
            lastProvider     = response.provider,
            lastModelId      = response.modelId,
            tier3Calls       = if (response.provider != "cache") current.tier3Calls + 1 else current.tier3Calls,
        )
    }

    /** Estimated USD cost for the session. Returns null for Ollama (free). */
    fun estimatedCostUsd(stats: UsageStats = _sessionStats.value): BigDecimal? {
        val rates = PRICING[stats.lastModelId] ?: return null
        val inputCost  = BigDecimal(stats.totalInputTokens)
            .multiply(rates.inputPer1M)
            .divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP)
        val outputCost = BigDecimal(stats.totalOutputTokens)
            .multiply(rates.outputPer1M)
            .divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP)
        return inputCost.add(outputCost)
    }

    /** Reset for a new session (e.g., app restart or settings navigation) */
    fun resetSession() {
        _sessionStats.value = UsageStats()
    }

    companion object {
        private data class PricingRate(val inputPer1M: BigDecimal, val outputPer1M: BigDecimal)

        private val PRICING = mapOf(
            "gemini-2.0-flash" to PricingRate(BigDecimal("0.075"), BigDecimal("0.30")),
            "gemini-2.5-pro"   to PricingRate(BigDecimal("1.25"),  BigDecimal("10.00")),
            "gpt-4o-mini"      to PricingRate(BigDecimal("0.15"),  BigDecimal("0.60")),
            "gpt-4o"           to PricingRate(BigDecimal("2.50"),  BigDecimal("10.00")),
            // Groq / Ollama → null = free, not tracked
        )
    }
}

data class UsageStats(
    val totalCalls        : Int    = 0,
    val totalInputTokens  : Int    = 0,
    val totalOutputTokens : Int    = 0,
    val totalLatencyMs    : Long   = 0L,
    val tier3Calls        : Int    = 0,
    val lastProvider      : String = "",
    val lastModelId       : String = "",
) {
    val totalTokens: Int      get() = totalInputTokens + totalOutputTokens
    val avgLatencyMs: Long    get() = if (totalCalls == 0) 0 else totalLatencyMs / totalCalls
    val cacheHitCount: Int    get() = totalCalls - tier3Calls
}
