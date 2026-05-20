package com.hisaab.di;

import com.hisaab.domain.agents.ActionAgent;
import com.hisaab.domain.agents.ContradictionAgent;
import com.hisaab.domain.agents.ForecastAgent;
import com.hisaab.domain.agents.HisaabAgentOrchestrator;
import com.hisaab.domain.agents.IngestionAgent;
import com.hisaab.domain.agents.InsightAgent;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class LlmModule_ProvideHisaabAgentOrchestratorFactory implements Factory<HisaabAgentOrchestrator> {
  private final Provider<IngestionAgent> ingestionAgentProvider;

  private final Provider<ContradictionAgent> contradictionAgentProvider;

  private final Provider<InsightAgent> insightAgentProvider;

  private final Provider<ActionAgent> actionAgentProvider;

  private final Provider<ForecastAgent> forecastAgentProvider;

  public LlmModule_ProvideHisaabAgentOrchestratorFactory(
      Provider<IngestionAgent> ingestionAgentProvider,
      Provider<ContradictionAgent> contradictionAgentProvider,
      Provider<InsightAgent> insightAgentProvider, Provider<ActionAgent> actionAgentProvider,
      Provider<ForecastAgent> forecastAgentProvider) {
    this.ingestionAgentProvider = ingestionAgentProvider;
    this.contradictionAgentProvider = contradictionAgentProvider;
    this.insightAgentProvider = insightAgentProvider;
    this.actionAgentProvider = actionAgentProvider;
    this.forecastAgentProvider = forecastAgentProvider;
  }

  @Override
  public HisaabAgentOrchestrator get() {
    return provideHisaabAgentOrchestrator(ingestionAgentProvider.get(), contradictionAgentProvider.get(), insightAgentProvider.get(), actionAgentProvider.get(), forecastAgentProvider.get());
  }

  public static LlmModule_ProvideHisaabAgentOrchestratorFactory create(
      Provider<IngestionAgent> ingestionAgentProvider,
      Provider<ContradictionAgent> contradictionAgentProvider,
      Provider<InsightAgent> insightAgentProvider, Provider<ActionAgent> actionAgentProvider,
      Provider<ForecastAgent> forecastAgentProvider) {
    return new LlmModule_ProvideHisaabAgentOrchestratorFactory(ingestionAgentProvider, contradictionAgentProvider, insightAgentProvider, actionAgentProvider, forecastAgentProvider);
  }

  public static HisaabAgentOrchestrator provideHisaabAgentOrchestrator(
      IngestionAgent ingestionAgent, ContradictionAgent contradictionAgent,
      InsightAgent insightAgent, ActionAgent actionAgent, ForecastAgent forecastAgent) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideHisaabAgentOrchestrator(ingestionAgent, contradictionAgent, insightAgent, actionAgent, forecastAgent));
  }
}
