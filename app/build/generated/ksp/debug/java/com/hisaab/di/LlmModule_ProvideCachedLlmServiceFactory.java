package com.hisaab.di;

import com.hisaab.domain.llm.CachedLlmService;
import com.hisaab.domain.llm.LlmResponseCache;
import com.hisaab.domain.llm.LlmService;
import com.hisaab.domain.llm.LlmUsageTracker;
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
public final class LlmModule_ProvideCachedLlmServiceFactory implements Factory<CachedLlmService> {
  private final Provider<LlmService> rawServiceProvider;

  private final Provider<LlmResponseCache> cacheProvider;

  private final Provider<LlmUsageTracker> usageTrackerProvider;

  public LlmModule_ProvideCachedLlmServiceFactory(Provider<LlmService> rawServiceProvider,
      Provider<LlmResponseCache> cacheProvider, Provider<LlmUsageTracker> usageTrackerProvider) {
    this.rawServiceProvider = rawServiceProvider;
    this.cacheProvider = cacheProvider;
    this.usageTrackerProvider = usageTrackerProvider;
  }

  @Override
  public CachedLlmService get() {
    return provideCachedLlmService(rawServiceProvider.get(), cacheProvider.get(), usageTrackerProvider.get());
  }

  public static LlmModule_ProvideCachedLlmServiceFactory create(
      Provider<LlmService> rawServiceProvider, Provider<LlmResponseCache> cacheProvider,
      Provider<LlmUsageTracker> usageTrackerProvider) {
    return new LlmModule_ProvideCachedLlmServiceFactory(rawServiceProvider, cacheProvider, usageTrackerProvider);
  }

  public static CachedLlmService provideCachedLlmService(LlmService rawService,
      LlmResponseCache cache, LlmUsageTracker usageTracker) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideCachedLlmService(rawService, cache, usageTracker));
  }
}
