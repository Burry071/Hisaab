package com.hisaab.di;

import com.hisaab.data.ingestion.GeminiParserFallback;
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
public final class LlmModule_ProvideGeminiParserFallbackFactory implements Factory<GeminiParserFallback> {
  private final Provider<CachedLlmService> llmServiceProvider;

  public LlmModule_ProvideGeminiParserFallbackFactory(
      Provider<CachedLlmService> llmServiceProvider) {
    this.llmServiceProvider = llmServiceProvider;
  }

  @Override
  public GeminiParserFallback get() {
    return provideGeminiParserFallback(llmServiceProvider.get());
  }

  public static LlmModule_ProvideGeminiParserFallbackFactory create(
      Provider<CachedLlmService> llmServiceProvider) {
    return new LlmModule_ProvideGeminiParserFallbackFactory(llmServiceProvider);
  }

  public static GeminiParserFallback provideGeminiParserFallback(CachedLlmService llmService) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideGeminiParserFallback(llmService));
  }
}
