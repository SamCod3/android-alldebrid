package com.samcod3.alldebrid.ui.screens.login;

import com.samcod3.alldebrid.data.api.DashboardApi;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
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
    "cast",
    "deprecation"
})
public final class ApiKeyManagerViewModel_Factory implements Factory<ApiKeyManagerViewModel> {
  private final Provider<DashboardApi> dashboardApiProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  public ApiKeyManagerViewModel_Factory(Provider<DashboardApi> dashboardApiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    this.dashboardApiProvider = dashboardApiProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
  }

  @Override
  public ApiKeyManagerViewModel get() {
    return newInstance(dashboardApiProvider.get(), settingsDataStoreProvider.get());
  }

  public static ApiKeyManagerViewModel_Factory create(Provider<DashboardApi> dashboardApiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    return new ApiKeyManagerViewModel_Factory(dashboardApiProvider, settingsDataStoreProvider);
  }

  public static ApiKeyManagerViewModel newInstance(DashboardApi dashboardApi,
      SettingsDataStore settingsDataStore) {
    return new ApiKeyManagerViewModel(dashboardApi, settingsDataStore);
  }
}
