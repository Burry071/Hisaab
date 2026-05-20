package com.hisaab.data.agent;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class HisaabAgentService_Factory implements Factory<HisaabAgentService> {
  @Override
  public HisaabAgentService get() {
    return newInstance();
  }

  public static HisaabAgentService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HisaabAgentService newInstance() {
    return new HisaabAgentService();
  }

  private static final class InstanceHolder {
    private static final HisaabAgentService_Factory INSTANCE = new HisaabAgentService_Factory();
  }
}
