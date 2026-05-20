package com.hisaab.presentation.viewmodels;

import com.hisaab.data.agent.HisaabAgentService;
import com.hisaab.data.local.TransactionDao;
import com.hisaab.domain.llm.CachedLlmService;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CachedLlmService> llmServiceProvider;

  private final Provider<HisaabAgentService> agentServiceProvider;

  private final Provider<TransactionDao> transactionDaoProvider;

  public HomeViewModel_Factory(Provider<CachedLlmService> llmServiceProvider,
      Provider<HisaabAgentService> agentServiceProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    this.llmServiceProvider = llmServiceProvider;
    this.agentServiceProvider = agentServiceProvider;
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(llmServiceProvider.get(), agentServiceProvider.get(), transactionDaoProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<CachedLlmService> llmServiceProvider,
      Provider<HisaabAgentService> agentServiceProvider,
      Provider<TransactionDao> transactionDaoProvider) {
    return new HomeViewModel_Factory(llmServiceProvider, agentServiceProvider, transactionDaoProvider);
  }

  public static HomeViewModel newInstance(CachedLlmService llmService,
      HisaabAgentService agentService, TransactionDao transactionDao) {
    return new HomeViewModel(llmService, agentService, transactionDao);
  }
}
