package com.hisaab.di;

import com.hisaab.domain.llm.LlmResponseCache;
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
public final class LlmModule_ProvideLlmResponseCacheFactory implements Factory<LlmResponseCache> {
  @Override
  public LlmResponseCache get() {
    return provideLlmResponseCache();
  }

  public static LlmModule_ProvideLlmResponseCacheFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LlmResponseCache provideLlmResponseCache() {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideLlmResponseCache());
  }

  private static final class InstanceHolder {
    private static final LlmModule_ProvideLlmResponseCacheFactory INSTANCE = new LlmModule_ProvideLlmResponseCacheFactory();
  }
}
