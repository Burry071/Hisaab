package com.hisaab.presentation.viewmodels;

import android.content.Context;
import com.hisaab.data.demo.DemoDataSeeder;
import com.hisaab.data.local.TransactionDao;
import com.hisaab.domain.agents.HisaabAgentOrchestrator;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class AgentViewModel_Factory implements Factory<AgentViewModel> {
  private final Provider<HisaabAgentOrchestrator> orchestratorProvider;

  private final Provider<DemoDataSeeder> demoDataSeederProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  private final Provider<Context> contextProvider;

  public AgentViewModel_Factory(Provider<HisaabAgentOrchestrator> orchestratorProvider,
      Provider<DemoDataSeeder> demoDataSeederProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<Context> contextProvider) {
    this.orchestratorProvider = orchestratorProvider;
    this.demoDataSeederProvider = demoDataSeederProvider;
    this.transactionDaoProvider = transactionDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AgentViewModel get() {
    return newInstance(orchestratorProvider.get(), demoDataSeederProvider.get(), transactionDaoProvider.get(), contextProvider.get());
  }

  public static AgentViewModel_Factory create(
      Provider<HisaabAgentOrchestrator> orchestratorProvider,
      Provider<DemoDataSeeder> demoDataSeederProvider,
      Provider<TransactionDao> transactionDaoProvider, Provider<Context> contextProvider) {
    return new AgentViewModel_Factory(orchestratorProvider, demoDataSeederProvider, transactionDaoProvider, contextProvider);
  }

  public static AgentViewModel newInstance(HisaabAgentOrchestrator orchestrator,
      DemoDataSeeder demoDataSeeder, TransactionDao transactionDao, Context context) {
    return new AgentViewModel(orchestrator, demoDataSeeder, transactionDao, context);
  }
}
