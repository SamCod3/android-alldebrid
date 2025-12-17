package com.samcod3.alldebrid.data.repository;

import com.samcod3.alldebrid.data.api.AllDebridApi;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class AllDebridRepository_Factory implements Factory<AllDebridRepository> {
  private final Provider<AllDebridApi> apiProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private final Provider<OkHttpClient> httpClientProvider;

  public AllDebridRepository_Factory(Provider<AllDebridApi> apiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<OkHttpClient> httpClientProvider) {
    this.apiProvider = apiProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public AllDebridRepository get() {
    return newInstance(apiProvider.get(), settingsDataStoreProvider.get(), httpClientProvider.get());
  }

  public static AllDebridRepository_Factory create(Provider<AllDebridApi> apiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<OkHttpClient> httpClientProvider) {
    return new AllDebridRepository_Factory(apiProvider, settingsDataStoreProvider, httpClientProvider);
  }

  public static AllDebridRepository newInstance(AllDebridApi api,
      SettingsDataStore settingsDataStore, OkHttpClient httpClient) {
    return new AllDebridRepository(api, settingsDataStore, httpClient);
  }
}
