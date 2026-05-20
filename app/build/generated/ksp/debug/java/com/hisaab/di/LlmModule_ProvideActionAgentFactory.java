package com.hisaab.di;

import com.hisaab.domain.agents.ActionAgent;
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
public final class LlmModule_ProvideActionAgentFactory implements Factory<ActionAgent> {
  @Override
  public ActionAgent get() {
    return provideActionAgent();
  }

  public static LlmModule_ProvideActionAgentFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ActionAgent provideActionAgent() {
    return Preconditions.checkNotNullFromProvides(LlmModule.INSTANCE.provideActionAgent());
  }

  private static final class InstanceHolder {
    private static final LlmModule_ProvideActionAgentFactory INSTANCE = new LlmModule_ProvideActionAgentFactory();
  }
}
