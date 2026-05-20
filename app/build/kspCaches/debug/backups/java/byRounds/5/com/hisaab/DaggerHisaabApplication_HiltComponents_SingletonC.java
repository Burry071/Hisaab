package com.hisaab;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.hisaab.data.agent.HisaabAgentService;
import com.hisaab.data.demo.DemoDataSeeder;
import com.hisaab.data.llm.LlmProviderRepository;
import com.hisaab.data.local.AppDatabase;
import com.hisaab.data.local.TransactionDao;
import com.hisaab.di.DatabaseModule_ProvideAppDatabaseFactory;
import com.hisaab.di.DatabaseModule_ProvideTransactionDaoFactory;
import com.hisaab.di.LlmModule_ProvideActionAgentFactory;
import com.hisaab.di.LlmModule_ProvideCachedLlmServiceFactory;
import com.hisaab.di.LlmModule_ProvideContradictionAgentFactory;
import com.hisaab.di.LlmModule_ProvideForecastAgentFactory;
import com.hisaab.di.LlmModule_ProvideHisaabAgentOrchestratorFactory;
import com.hisaab.di.LlmModule_ProvideIngestionAgentFactory;
import com.hisaab.di.LlmModule_ProvideInsightAgentFactory;
import com.hisaab.di.LlmModule_ProvideLlmProviderRepositoryFactory;
import com.hisaab.di.LlmModule_ProvideLlmResponseCacheFactory;
import com.hisaab.di.LlmModule_ProvideLlmUsageTrackerFactory;
import com.hisaab.di.LlmModule_ProvideRawLlmServiceFactory;
import com.hisaab.domain.agents.ActionAgent;
import com.hisaab.domain.agents.ContradictionAgent;
import com.hisaab.domain.agents.ForecastAgent;
import com.hisaab.domain.agents.HisaabAgentOrchestrator;
import com.hisaab.domain.agents.IngestionAgent;
import com.hisaab.domain.agents.InsightAgent;
import com.hisaab.domain.llm.CachedLlmService;
import com.hisaab.domain.llm.LlmHealthMonitor;
import com.hisaab.domain.llm.LlmResponseCache;
import com.hisaab.domain.llm.LlmService;
import com.hisaab.domain.llm.LlmUsageTracker;
import com.hisaab.presentation.viewmodels.AddTransactionViewModel;
import com.hisaab.presentation.viewmodels.AddTransactionViewModel_HiltModules;
import com.hisaab.presentation.viewmodels.AgentViewModel;
import com.hisaab.presentation.viewmodels.AgentViewModel_HiltModules;
import com.hisaab.presentation.viewmodels.HomeViewModel;
import com.hisaab.presentation.viewmodels.HomeViewModel_HiltModules;
import com.hisaab.presentation.viewmodels.InsightsViewModel;
import com.hisaab.presentation.viewmodels.InsightsViewModel_HiltModules;
import com.hisaab.presentation.viewmodels.SettingsViewModel;
import com.hisaab.presentation.viewmodels.SettingsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerHisaabApplication_HiltComponents_SingletonC {
  private DaggerHisaabApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public HisaabApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements HisaabApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements HisaabApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements HisaabApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements HisaabApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements HisaabApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements HisaabApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements HisaabApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public HisaabApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends HisaabApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends HisaabApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends HisaabApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends HisaabApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    private DemoDataSeeder demoDataSeeder() {
      return new DemoDataSeeder(singletonCImpl.transactionDao());
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_AddTransactionViewModel, AddTransactionViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_AgentViewModel, AgentViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_HomeViewModel, HomeViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_InsightsViewModel, InsightsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectHealthMonitor(instance, singletonCImpl.llmHealthMonitorProvider.get());
      MainActivity_MembersInjector.injectUsageTracker(instance, singletonCImpl.provideLlmUsageTrackerProvider.get());
      MainActivity_MembersInjector.injectDemoDataSeeder(instance, demoDataSeeder());
      return instance;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_hisaab_presentation_viewmodels_AgentViewModel = "com.hisaab.presentation.viewmodels.AgentViewModel";

      static String com_hisaab_presentation_viewmodels_HomeViewModel = "com.hisaab.presentation.viewmodels.HomeViewModel";

      static String com_hisaab_presentation_viewmodels_AddTransactionViewModel = "com.hisaab.presentation.viewmodels.AddTransactionViewModel";

      static String com_hisaab_presentation_viewmodels_SettingsViewModel = "com.hisaab.presentation.viewmodels.SettingsViewModel";

      static String com_hisaab_presentation_viewmodels_InsightsViewModel = "com.hisaab.presentation.viewmodels.InsightsViewModel";

      @KeepFieldType
      AgentViewModel com_hisaab_presentation_viewmodels_AgentViewModel2;

      @KeepFieldType
      HomeViewModel com_hisaab_presentation_viewmodels_HomeViewModel2;

      @KeepFieldType
      AddTransactionViewModel com_hisaab_presentation_viewmodels_AddTransactionViewModel2;

      @KeepFieldType
      SettingsViewModel com_hisaab_presentation_viewmodels_SettingsViewModel2;

      @KeepFieldType
      InsightsViewModel com_hisaab_presentation_viewmodels_InsightsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends HisaabApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddTransactionViewModel> addTransactionViewModelProvider;

    private Provider<AgentViewModel> agentViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<InsightsViewModel> insightsViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private DemoDataSeeder demoDataSeeder() {
      return new DemoDataSeeder(singletonCImpl.transactionDao());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addTransactionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.agentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.insightsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_AddTransactionViewModel, ((Provider) addTransactionViewModelProvider)).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_AgentViewModel, ((Provider) agentViewModelProvider)).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_HomeViewModel, ((Provider) homeViewModelProvider)).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_InsightsViewModel, ((Provider) insightsViewModelProvider)).put(LazyClassKeyProvider.com_hisaab_presentation_viewmodels_SettingsViewModel, ((Provider) settingsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_hisaab_presentation_viewmodels_AgentViewModel = "com.hisaab.presentation.viewmodels.AgentViewModel";

      static String com_hisaab_presentation_viewmodels_AddTransactionViewModel = "com.hisaab.presentation.viewmodels.AddTransactionViewModel";

      static String com_hisaab_presentation_viewmodels_InsightsViewModel = "com.hisaab.presentation.viewmodels.InsightsViewModel";

      static String com_hisaab_presentation_viewmodels_SettingsViewModel = "com.hisaab.presentation.viewmodels.SettingsViewModel";

      static String com_hisaab_presentation_viewmodels_HomeViewModel = "com.hisaab.presentation.viewmodels.HomeViewModel";

      @KeepFieldType
      AgentViewModel com_hisaab_presentation_viewmodels_AgentViewModel2;

      @KeepFieldType
      AddTransactionViewModel com_hisaab_presentation_viewmodels_AddTransactionViewModel2;

      @KeepFieldType
      InsightsViewModel com_hisaab_presentation_viewmodels_InsightsViewModel2;

      @KeepFieldType
      SettingsViewModel com_hisaab_presentation_viewmodels_SettingsViewModel2;

      @KeepFieldType
      HomeViewModel com_hisaab_presentation_viewmodels_HomeViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.hisaab.presentation.viewmodels.AddTransactionViewModel 
          return (T) new AddTransactionViewModel(singletonCImpl.transactionDao());

          case 1: // com.hisaab.presentation.viewmodels.AgentViewModel 
          return (T) new AgentViewModel(singletonCImpl.provideHisaabAgentOrchestratorProvider.get(), viewModelCImpl.demoDataSeeder(), singletonCImpl.transactionDao(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.hisaab.presentation.viewmodels.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.provideCachedLlmServiceProvider.get(), singletonCImpl.hisaabAgentServiceProvider.get(), singletonCImpl.transactionDao());

          case 3: // com.hisaab.presentation.viewmodels.InsightsViewModel 
          return (T) new InsightsViewModel();

          case 4: // com.hisaab.presentation.viewmodels.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideLlmProviderRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends HisaabApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends HisaabApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends HisaabApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<LlmProviderRepository> provideLlmProviderRepositoryProvider;

    private Provider<LlmService> provideRawLlmServiceProvider;

    private Provider<LlmResponseCache> provideLlmResponseCacheProvider;

    private Provider<LlmUsageTracker> provideLlmUsageTrackerProvider;

    private Provider<CachedLlmService> provideCachedLlmServiceProvider;

    private Provider<LlmHealthMonitor> llmHealthMonitorProvider;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<IngestionAgent> provideIngestionAgentProvider;

    private Provider<ContradictionAgent> provideContradictionAgentProvider;

    private Provider<InsightAgent> provideInsightAgentProvider;

    private Provider<ActionAgent> provideActionAgentProvider;

    private Provider<ForecastAgent> provideForecastAgentProvider;

    private Provider<HisaabAgentOrchestrator> provideHisaabAgentOrchestratorProvider;

    private Provider<HisaabAgentService> hisaabAgentServiceProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private TransactionDao transactionDao() {
      return DatabaseModule_ProvideTransactionDaoFactory.provideTransactionDao(provideAppDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideLlmProviderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LlmProviderRepository>(singletonCImpl, 3));
      this.provideRawLlmServiceProvider = DoubleCheck.provider(new SwitchingProvider<LlmService>(singletonCImpl, 2));
      this.provideLlmResponseCacheProvider = DoubleCheck.provider(new SwitchingProvider<LlmResponseCache>(singletonCImpl, 4));
      this.provideLlmUsageTrackerProvider = DoubleCheck.provider(new SwitchingProvider<LlmUsageTracker>(singletonCImpl, 5));
      this.provideCachedLlmServiceProvider = DoubleCheck.provider(new SwitchingProvider<CachedLlmService>(singletonCImpl, 1));
      this.llmHealthMonitorProvider = DoubleCheck.provider(new SwitchingProvider<LlmHealthMonitor>(singletonCImpl, 0));
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 6));
      this.provideIngestionAgentProvider = DoubleCheck.provider(new SwitchingProvider<IngestionAgent>(singletonCImpl, 8));
      this.provideContradictionAgentProvider = DoubleCheck.provider(new SwitchingProvider<ContradictionAgent>(singletonCImpl, 9));
      this.provideInsightAgentProvider = DoubleCheck.provider(new SwitchingProvider<InsightAgent>(singletonCImpl, 10));
      this.provideActionAgentProvider = DoubleCheck.provider(new SwitchingProvider<ActionAgent>(singletonCImpl, 11));
      this.provideForecastAgentProvider = DoubleCheck.provider(new SwitchingProvider<ForecastAgent>(singletonCImpl, 12));
      this.provideHisaabAgentOrchestratorProvider = DoubleCheck.provider(new SwitchingProvider<HisaabAgentOrchestrator>(singletonCImpl, 7));
      this.hisaabAgentServiceProvider = DoubleCheck.provider(new SwitchingProvider<HisaabAgentService>(singletonCImpl, 13));
    }

    @Override
    public void injectHisaabApplication(HisaabApplication hisaabApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.hisaab.domain.llm.LlmHealthMonitor 
          return (T) new LlmHealthMonitor(singletonCImpl.provideCachedLlmServiceProvider.get());

          case 1: // com.hisaab.domain.llm.CachedLlmService 
          return (T) LlmModule_ProvideCachedLlmServiceFactory.provideCachedLlmService(singletonCImpl.provideRawLlmServiceProvider.get(), singletonCImpl.provideLlmResponseCacheProvider.get(), singletonCImpl.provideLlmUsageTrackerProvider.get());

          case 2: // com.hisaab.domain.llm.LlmService 
          return (T) LlmModule_ProvideRawLlmServiceFactory.provideRawLlmService(singletonCImpl.provideLlmProviderRepositoryProvider.get());

          case 3: // com.hisaab.data.llm.LlmProviderRepository 
          return (T) LlmModule_ProvideLlmProviderRepositoryFactory.provideLlmProviderRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.hisaab.domain.llm.LlmResponseCache 
          return (T) LlmModule_ProvideLlmResponseCacheFactory.provideLlmResponseCache();

          case 5: // com.hisaab.domain.llm.LlmUsageTracker 
          return (T) LlmModule_ProvideLlmUsageTrackerFactory.provideLlmUsageTracker();

          case 6: // com.hisaab.data.local.AppDatabase 
          return (T) DatabaseModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.hisaab.domain.agents.HisaabAgentOrchestrator 
          return (T) LlmModule_ProvideHisaabAgentOrchestratorFactory.provideHisaabAgentOrchestrator(singletonCImpl.provideIngestionAgentProvider.get(), singletonCImpl.provideContradictionAgentProvider.get(), singletonCImpl.provideInsightAgentProvider.get(), singletonCImpl.provideActionAgentProvider.get(), singletonCImpl.provideForecastAgentProvider.get());

          case 8: // com.hisaab.domain.agents.IngestionAgent 
          return (T) LlmModule_ProvideIngestionAgentFactory.provideIngestionAgent();

          case 9: // com.hisaab.domain.agents.ContradictionAgent 
          return (T) LlmModule_ProvideContradictionAgentFactory.provideContradictionAgent(singletonCImpl.provideCachedLlmServiceProvider.get());

          case 10: // com.hisaab.domain.agents.InsightAgent 
          return (T) LlmModule_ProvideInsightAgentFactory.provideInsightAgent(singletonCImpl.provideCachedLlmServiceProvider.get());

          case 11: // com.hisaab.domain.agents.ActionAgent 
          return (T) LlmModule_ProvideActionAgentFactory.provideActionAgent();

          case 12: // com.hisaab.domain.agents.ForecastAgent 
          return (T) LlmModule_ProvideForecastAgentFactory.provideForecastAgent();

          case 13: // com.hisaab.data.agent.HisaabAgentService 
          return (T) new HisaabAgentService();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
