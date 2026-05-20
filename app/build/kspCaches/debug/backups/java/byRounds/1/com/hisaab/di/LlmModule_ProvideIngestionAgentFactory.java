package com.hisaab.di;

import com.hisaab.domain.agents.IngestionAgent;
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
public final class LlmModule_ProvideIngestionAgentFactory implements Factory<IngestionAgent> {
  @Override
  public IngestionAgent get() {
    return provideIngestionAgent();
  }

  public static LlmModule_ProvideIngestionAgentFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static IngestionAgent provideIngestionAgent() {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideIngestionAgent());
  }

  private static final class InstanceHolder {
    private static final LlmModule_ProvideIngestionAgentFactory INSTANCE = new LlmModule_ProvideIngestionAgentFactory();
  }
}
