package com.hisaab.domain.llm

import java.math.BigDecimal

/**
 * Sealed class representing all supported LLM backend options.
 *
 * User picks one in SettingsScreen; their choice is persisted in DataStore.
 * The [LlmServiceFactory] reads this and returns the correct [LlmService].
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  PROVIDER                 │ NEEDS KEY │ OFFLINE │ PRIVACY           │
 * ├──────────────────────────────────────────────────────────────────────┤
 * │  GeminiProvider           │  Yes      │  No     │  Data sent to GCP │
 * │  OpenAICompatibleProvider │  Yes      │  No     │  Depends on host  │
 * │  OllamaProvider           │  No       │  Yes ✅ │  100% on-device ✅ │
 * └──────────────────────────────────────────────────────────────────────┘
 */
sealed class LlmProvider {

    /** Display name shown in SettingsScreen provider picker */
    abstract val displayName: String

    /** Short description shown as subtitle in the picker card */
    abstract val description: String

    /** Icon resource name (maps to drawable) */
    abstract val iconName: String

    /** Whether this provider requires a network connection */
    abstract val requiresNetwork: Boolean

    /** Whether this provider stores data on an external server */
    abstract val isPrivacySensitive: Boolean

    // ─────────────────────────────────────────────────────────────────────

    /**
     * Google Gemini API — default provider.
     * User can supply their own key or leave blank to use Hisaab's shared key
     * (rate-limited in production; user's own key gets priority quota).
     *
     * Models supported: gemini-2.0-flash, gemini-2.5-pro
     */
    data class GeminiProvider(
        val apiKey: String = "",          // blank = use Hisaab's pooled key
        val model: GeminiModel = GeminiModel.FLASH,
    ) : LlmProvider() {
        override val displayName      = "Google Gemini"
        override val description      = "Fastest inference. Powered by Google AI."
        override val iconName         = "ic_gemini"
        override val requiresNetwork  = true
        override val isPrivacySensitive = true

        val resolvedKey: String get() = apiKey.ifBlank { "TODO_ADD_API_KEY" }
    }

    /**
     * OpenAI-compatible endpoint — works with:
     *   • OpenAI (api.openai.com)
     *   • Groq (api.groq.com/openai/v1)
     *   • Together AI
     *   • Any self-hosted vLLM / LM Studio with an OpenAI-compatible API
     */
    data class OpenAICompatibleProvider(
        val apiKey: String,
        val baseUrl: String = "https://api.openai.com/v1",
        val model: String   = "gpt-4o-mini",
    ) : LlmProvider() {
        override val displayName      = "OpenAI / Custom"
        override val description      = "Use your own OpenAI key or any compatible endpoint."
        override val iconName         = "ic_openai"
        override val requiresNetwork  = true
        override val isPrivacySensitive = true
    }

    /**
     * Ollama — 100% local, fully offline, zero data leaves the device.
     *
     * Default baseUrl assumes Termux-hosted Ollama on Android:
     *   http://127.0.0.1:11434
     *
     * Popular Pakistani-friendly models (small enough to run on mid-range phones):
     *   • phi3:mini       (3.8B — fast on 4GB RAM)
     *   • gemma2:2b       (2B  — very fast)
     *   • llama3.2:3b     (3B  — good reasoning)
     */
    data class OllamaProvider(
        val baseUrl: String = "http://127.0.0.1:11434",
        val model: String   = "phi3:mini",
    ) : LlmProvider() {
        override val displayName       = "Local Model (Ollama)"
        override val description       = "100% offline. Your data never leaves your phone."
        override val iconName          = "ic_ollama"
        override val requiresNetwork   = false
        override val isPrivacySensitive = false
    }
}

enum class GeminiModel(val modelId: String, val label: String) {
    FLASH("gemini-1.5-flash-latest", "Gemini 1.5 Flash"),
    FLASH_2_5("gemini-2.5-flash",    "Gemini 2.5 Flash"),
    PRO  ("gemini-1.5-pro-latest",   "Gemini 1.5 Pro"),
}

/**
 * Persisted config envelope — this is what gets saved to DataStore.
 */
data class LlmProviderConfig(
    val provider: LlmProvider,
    val isVerified: Boolean = false,   // true after a successful test call
    val lastVerifiedAtMs: Long? = null,
) {
    companion object {
        /** Default: Gemini Flash with pooled key */
        val Default = LlmProviderConfig(
            provider   = LlmProvider.GeminiProvider(),
            isVerified = false,
        )
    }
}
