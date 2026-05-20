package com.hisaab.di;

import com.hisaab.domain.agents.ForecastAgent;
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
public final class LlmModule_ProvideForecastAgentFactory implements Factory<ForecastAgent> {
  @Override
  public ForecastAgent get() {
    return provideForecastAgent();
  }

  public static LlmModule_ProvideForecastAgentFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ForecastAgent provideForecastAgent() {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideForecastAgent());
  }

  private static final class InstanceHolder {
    private static final LlmModule_ProvideForecastAgentFactory INSTANCE = new LlmModule_ProvideForecastAgentFactory();
  }
}
