package com.hisaab.di;

import com.hisaab.domain.agents.InsightAgent;
import com.hisaab.domain.llm.CachedLlmService;
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
public final class LlmModule_ProvideInsightAgentFactory implements Factory<InsightAgent> {
  private final Provider<CachedLlmService> llmServiceProvider;

  public LlmModule_ProvideInsightAgentFactory(Provider<CachedLlmService> llmServiceProvider) {
    this.llmServiceProvider = llmServiceProvider;
  }

  @Override
  public InsightAgent get() {
    return provideInsightAgent(llmServiceProvider.get());
  }

  public static LlmModule_ProvideInsightAgentFactory create(
      Provider<CachedLlmService> llmServiceProvider) {
    return new LlmModule_ProvideInsightAgentFactory(llmServiceProvider);
  }

  public static InsightAgent provideInsightAgent(CachedLlmService llmService) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideInsightAgent(llmService));
  }
}
