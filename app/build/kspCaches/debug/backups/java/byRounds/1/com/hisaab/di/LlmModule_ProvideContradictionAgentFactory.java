package com.hisaab.di;

import com.hisaab.domain.agents.ContradictionAgent;
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
public final class LlmModule_ProvideContradictionAgentFactory implements Factory<ContradictionAgent> {
  private final Provider<CachedLlmService> llmServiceProvider;

  public LlmModule_ProvideContradictionAgentFactory(Provider<CachedLlmService> llmServiceProvider) {
    this.llmServiceProvider = llmServiceProvider;
  }

  @Override
  public ContradictionAgent get() {
    return provideContradictionAgent(llmServiceProvider.get());
  }

  public static LlmModule_ProvideContradictionAgentFactory create(
      Provider<CachedLlmService> llmServiceProvider) {
    return new LlmModule_ProvideContradictionAgentFactory(llmServiceProvider);
  }

  public static ContradictionAgent provideContradictionAgent(CachedLlmService llmService) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideContradictionAgent(llmService));
  }
}
