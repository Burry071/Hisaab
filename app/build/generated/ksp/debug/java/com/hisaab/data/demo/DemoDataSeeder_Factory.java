package com.hisaab.data.demo;

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
public final class DemoDataSeeder_Factory implements Factory<DemoDataSeeder> {
  private final Provider<TransactionDao> transactionDaoProvider;

  public DemoDataSeeder_Factory(Provider<TransactionDao> transactionDaoProvider) {
    this.transactionDaoProvider = transactionDaoProvider;
  }

  @Override
  public DemoDataSeeder get() {
    return newInstance(transactionDaoProvider.get());
  }

  public static DemoDataSeeder_Factory create(Provider<TransactionDao> transactionDaoProvider) {
    return new DemoDataSeeder_Factory(transactionDaoProvider);
  }

  public static DemoDataSeeder newInstance(TransactionDao transactionDao) {
    return new DemoDataSeeder(transactionDao);
  }
}
