package com.hisaab.data.llm

import com.hisaab.domain.llm.LlmProvider
import com.hisaab.domain.llm.LlmService

/**
 * Factory — returns the correct [LlmService] implementation for the given [LlmProvider].
 *
 * The agent orchestrator and GeminiParserFallback should receive LlmService
 * via dependency injection (Hilt), not construct it directly.
 *
 * DI binding (in LlmModule.kt / AppModule.kt):
 *   @Provides @Singleton
 *   fun provideLlmService(repo: LlmProviderRepository): LlmService =
 *       LlmServiceFactory.create(repo.getActiveProvider())
 */
object LlmServiceFactory {

    fun create(provider: LlmProvider): LlmService = when (provider) {
        is LlmProvider.GeminiProvider           -> GeminiLlmService(provider)
        is LlmProvider.OpenAICompatibleProvider -> OpenAICompatibleLlmService(provider)
        is LlmProvider.OllamaProvider           -> OllamaLlmService(provider)
    }
}
