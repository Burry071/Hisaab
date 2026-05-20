package com.hisaab.presentation.viewmodels;

import com.hisaab.data.local.TransactionDao;
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
public final class AddTransactionViewModel_Factory implements Factory<AddTransactionViewModel> {
  private final Provider<TransactionDao> transactionDaoProvider;

  public AddTransactionViewModel_Factory(Provider<TransactionDao> transactionDaoProvider) {
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public AddTransactionViewModel get() {
    return newInstance(transactionDaoProvider.get());
  }

  public static AddTransactionViewModel_Factory create(
      Provider<TransactionDao> transactionDaoProvider) {
    return new AddTransactionViewModel_Factory(transactionDaoProvider);
  }

  public static AddTransactionViewModel newInstance(TransactionDao transactionDao) {
    return new AddTransactionViewModel(transactionDao);
  }
}
