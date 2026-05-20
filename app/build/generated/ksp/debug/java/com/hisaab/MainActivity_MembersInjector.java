package com.hisaab;

import com.hisaab.data.demo.DemoDataSeeder;
import com.hisaab.domain.llm.LlmHealthMonitor;
import com.hisaab.domain.llm.LlmUsageTracker;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<LlmHealthMonitor> healthMonitorProvider;

  private final Provider<LlmUsageTracker> usageTrackerProvider;

  private final Provider<DemoDataSeeder> demoDataSeederProvider;

  public MainActivity_MembersInjector(Provider<LlmHealthMonitor> healthMonitorProvider,
      Provider<LlmUsageTracker> usageTrackerProvider,
      Provider<DemoDataSeeder> demoDataSeederProvider) {
    this.healthMonitorProvider = healthMonitorProvider;
    this.usageTrackerProvider = usageTrackerProvider;
    this.demoDataSeederProvider = demoDataSeederProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<LlmHealthMonitor> healthMonitorProvider,
      Provider<LlmUsageTracker> usageTrackerProvider,
      Provider<DemoDataSeeder> demoDataSeederProvider) {
    return new MainActivity_MembersInjector(healthMonitorProvider, usageTrackerProvider, demoDataSeederProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectHealthMonitor(instance, healthMonitorProvider.get());
    injectUsageTracker(instance, usageTrackerProvider.get());
    injectDemoDataSeeder(instance, demoDataSeederProvider.get());
  }

  @InjectedFieldSignature("com.hisaab.MainActivity.healthMonitor")
  public static void injectHealthMonitor(MainActivity instance, LlmHealthMonitor healthMonitor) {
    instance.healthMonitor = healthMonitor;
  }

  @InjectedFieldSignature("com.hisaab.MainActivity.usageTracker")
  public static void injectUsageTracker(MainActivity instance, LlmUsageTracker usageTracker) {
    instance.usageTracker = usageTracker;
  }

  @InjectedFieldSignature("com.hisaab.MainActivity.demoDataSeeder")
  public static void injectDemoDataSeeder(MainActivity instance, DemoDataSeeder demoDataSeeder) {
    instance.demoDataSeeder = demoDataSeeder;
  }
}
