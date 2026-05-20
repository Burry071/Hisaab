package com.hisaab.di;

import com.hisaab.data.llm.LlmProviderRepository;
import com.hisaab.domain.llm.LlmService;
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
public final class LlmModule_ProvideRawLlmServiceFactory implements Factory<LlmService> {
  private final Provider<LlmProviderRepository> repoProvider;

  public LlmModule_ProvideRawLlmServiceFactory(Provider<LlmProviderRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public LlmService get() {
    return provideRawLlmService(repoProvider.get());
  }

  public static LlmModule_ProvideRawLlmServiceFactory create(
      Provider<LlmProviderRepository> repoProvider) {
    return new LlmModule_ProvideRawLlmServiceFactory(repoProvider);
  }

  public static LlmService provideRawLlmService(LlmProviderRepository repo) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideRawLlmService(repo));
  }
}
