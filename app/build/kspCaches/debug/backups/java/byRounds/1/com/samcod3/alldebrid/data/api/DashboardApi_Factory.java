package com.samcod3.alldebrid.data.api;

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
    "cast",
    "deprecation"
})
public final class DashboardApi_Factory implements Factory<DashboardApi> {
  @Override
  public DashboardApi get() {
    return newInstance();
  }

  public static DashboardApi_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DashboardApi newInstance() {
    return new DashboardApi();
  }

  private static final class InstanceHolder {
    private static final DashboardApi_Factory INSTANCE = new DashboardApi_Factory();
  }
}
