package com.samcod3.alldebrid.ui.screens.settings;

import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import com.samcod3.alldebrid.data.repository.AllDebridRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private final Provider<AllDebridRepository> repositoryProvider;

  public SettingsViewModel_Factory(Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<AllDebridRepository> repositoryProvider) {
    this.settingsDataStoreProvider = settingsDataStoreProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsDataStoreProvider.get(), repositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<AllDebridRepository> repositoryProvider) {
    return new SettingsViewModel_Factory(settingsDataStoreProvider, repositoryProvider);
  }

  public static SettingsViewModel newInstance(SettingsDataStore settingsDataStore,
      AllDebridRepository repository) {
    return new SettingsViewModel(settingsDataStore, repository);
  }
}
