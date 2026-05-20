package com.hisaab.presentation.viewmodels;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class InsightsViewModel_Factory implements Factory<InsightsViewModel> {
  @Override
  public InsightsViewModel get() {
    return newInstance();
  }

  public static InsightsViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InsightsViewModel newInstance() {
    return new InsightsViewModel();
  }

  private static final class InstanceHolder {
    private static final InsightsViewModel_Factory INSTANCE = new InsightsViewModel_Factory();
  }
}
