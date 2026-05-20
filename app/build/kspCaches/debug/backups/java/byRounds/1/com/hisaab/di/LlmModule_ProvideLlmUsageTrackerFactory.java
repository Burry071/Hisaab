package com.hisaab.di;

import com.hisaab.domain.llm.LlmUsageTracker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class LlmModule_ProvideLlmUsageTrackerFactory implements Factory<LlmUsageTracker> {
  @Override
  public LlmUsageTracker get() {
    return provideLlmUsageTracker();
  }

  public static LlmModule_ProvideLlmUsageTrackerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LlmUsageTracker provideLlmUsageTracker() {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideLlmUsageTracker());
  }

  private static final class InstanceHolder {
    private static final LlmModule_ProvideLlmUsageTrackerFactory INSTANCE = new LlmModule_ProvideLlmUsageTrackerFactory();
  }
}
