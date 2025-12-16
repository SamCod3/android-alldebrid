package com.samcod3.alldebrid.data.repository;

import com.samcod3.alldebrid.data.api.JackettApi;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class JackettRepository_Factory implements Factory<JackettRepository> {
  private final Provider<JackettApi> apiProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  public JackettRepository_Factory(Provider<JackettApi> apiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    this.apiProvider = apiProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
  }

  @Override
  public JackettRepository get() {
    return newInstance(apiProvider.get(), settingsDataStoreProvider.get());
  }

  public static JackettRepository_Factory create(Provider<JackettApi> apiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider) {
    return new JackettRepository_Factory(apiProvider, settingsDataStoreProvider);
  }

  public static JackettRepository newInstance(JackettApi api, SettingsDataStore settingsDataStore) {
    return new JackettRepository(api, settingsDataStore);
  }
}
