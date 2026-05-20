package com.hisaab.di;

import android.content.Context;
import com.hisaab.data.llm.LlmProviderRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class LlmModule_ProvideLlmProviderRepositoryFactory implements Factory<LlmProviderRepository> {
  private final Provider<Context> contextProvider;

  public LlmModule_ProvideLlmProviderRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LlmProviderRepository get() {
    return provideLlmProviderRepository(contextProvider.get());
  }

  public static LlmModule_ProvideLlmProviderRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new LlmModule_ProvideLlmProviderRepositoryFactory(contextProvider);
  }

  public static LlmProviderRepository provideLlmProviderRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideLlmProviderRepository(context));
  }
}
