package com.hisaab.di

import android.content.Context
import com.hisaab.data.ingestion.GeminiParserFallback
import com.hisaab.data.llm.LlmProviderRepository
import com.hisaab.data.llm.LlmServiceFactory
import com.hisaab.domain.agents.*
import com.hisaab.domain.llm.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module — wires the entire LLM service layer.
 *
 * Dependency graph:
 *
 *   LlmProviderRepository (DataStore)
 *       ↓
 *   LlmServiceFactory.create(provider)   →  GeminiLlmService | OpenAICompatibleLlmService | OllamaLlmService
 *       ↓
 *   CachedLlmService  (wraps raw service with cache + usage tracking)
 *       ↓
 *   GeminiParserFallback  (Tier 3 parser)
 *   InsightAgent          (LLM-enhanced reasoning)
 *   ContradictionAgent    (LLM arbitration)
 *       ↓
 *   HisaabAgentOrchestrator
 */
@Module
@InstallIn(SingletonComponent::class)
object LlmModule {

    // ── Repository ─────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideLlmProviderRepository(
        @ApplicationContext context: Context,
    ): LlmProviderRepository = LlmProviderRepository(context)

    // ── Infrastructure ─────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideLlmResponseCache(): LlmResponseCache = LlmResponseCache()

    @Provides @Singleton
    fun provideLlmUsageTracker(): LlmUsageTracker = LlmUsageTracker()

    // ── Raw LLM service (built from persisted provider config) ─────────────────

    @Provides @Singleton
    fun provideRawLlmService(
        repo: LlmProviderRepository,
    ): LlmService {
        // Read saved provider synchronously at app start.
        // Subsequent changes in Settings invalidate this via restart or dynamic reload.
        val provider = repo.currentConfig.provider
        return LlmServiceFactory.create(provider)
    }

    // ── Cached + tracked LLM service (what all agents use) ────────────────────

    @Provides @Singleton
    fun provideCachedLlmService(
        rawService   : LlmService,
        cache        : LlmResponseCache,
        usageTracker : LlmUsageTracker,
    ): CachedLlmService = CachedLlmService(rawService, cache, usageTracker)

    // ── Tier 3 parser fallback ─────────────────────────────────────────────────

    @Provides @Singleton
    fun provideGeminiParserFallback(
        llmService: CachedLlmService,
    ): GeminiParserFallback = GeminiParserFallback(llmService)

    // ── 5 Agents ───────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideIngestionAgent(): IngestionAgent = IngestionAgent()

    @Provides @Singleton
    fun provideContradictionAgent(
        llmService: CachedLlmService,
    ): ContradictionAgent = ContradictionAgent(llmService)

    @Provides @Singleton
    fun provideInsightAgent(
        llmService: CachedLlmService,
    ): InsightAgent = InsightAgent(llmService)

    @Provides @Singleton
    fun provideActionAgent(): ActionAgent = ActionAgent()

    @Provides @Singleton
    fun provideForecastAgent(): ForecastAgent = ForecastAgent()

    // ── Orchestrator ────────────────────────────────────────────────────────────

    @Provides @Singleton
    fun provideHisaabAgentOrchestrator(
        ingestionAgent    : IngestionAgent,
        contradictionAgent: ContradictionAgent,
        insightAgent      : InsightAgent,
        actionAgent       : ActionAgent,
        forecastAgent     : ForecastAgent,
    ): HisaabAgentOrchestrator = HisaabAgentOrchestrator(
        ingestionAgent,
        contradictionAgent,
        insightAgent,
        actionAgent,
        forecastAgent,
    )
}
