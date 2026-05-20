package com.hisaab.presentation.viewmodels;

import com.hisaab.data.llm.LlmProviderRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<LlmProviderRepository> repoProvider;

  public SettingsViewModel_Factory(Provider<LlmProviderRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<LlmProviderRepository> repoProvider) {
    return new SettingsViewModel_Factory(repoProvider);
  }

  public static SettingsViewModel newInstance(LlmProviderRepository repo) {
    return new SettingsViewModel(repo);
  }
}
