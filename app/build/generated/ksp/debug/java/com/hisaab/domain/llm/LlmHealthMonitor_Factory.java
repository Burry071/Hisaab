package com.hisaab.domain.llm;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LlmHealthMonitor_Factory implements Factory<LlmHealthMonitor> {
  private final Provider<CachedLlmService> llmServiceProvider;

  public LlmHealthMonitor_Factory(Provider<CachedLlmService> llmServiceProvider) {
    this.llmServiceProvider = llmServiceProvider;
  }

  @Override
  public LlmHealthMonitor get() {
    return newInstance(llmServiceProvider.get());
  }

  public static LlmHealthMonitor_Factory create(Provider<CachedLlmService> llmServiceProvider) {
    return new LlmHealthMonitor_Factory(llmServiceProvider);
  }

  public static LlmHealthMonitor newInstance(CachedLlmService llmService) {
    return new LlmHealthMonitor(llmService);
  }
}
