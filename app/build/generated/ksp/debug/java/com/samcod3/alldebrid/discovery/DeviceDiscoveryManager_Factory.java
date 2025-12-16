package com.samcod3.alldebrid.discovery;

import android.content.Context;
import com.samcod3.alldebrid.data.api.KodiApi;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
    "cast",
    "deprecation"
})
public final class DeviceDiscoveryManager_Factory implements Factory<DeviceDiscoveryManager> {
  private final Provider<Context> contextProvider;

  private final Provider<KodiApi> kodiApiProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  public DeviceDiscoveryManager_Factory(Provider<Context> contextProvider,
      Provider<KodiApi> kodiApiProvider, Provider<SettingsDataStore> settingsDataStoreProvider) {
    this.contextProvider = contextProvider;
    this.kodiApiProvider = kodiApiProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
  }

  @Override
  public DeviceDiscoveryManager get() {
    return newInstance(contextProvider.get(), kodiApiProvider.get(), settingsDataStoreProvider.get());
  }

  public static DeviceDiscoveryManager_Factory create(Provider<Context> contextProvider,
      Provider<KodiApi> kodiApiProvider, Provider<SettingsDataStore> settingsDataStoreProvider) {
    return new DeviceDiscoveryManager_Factory(contextProvider, kodiApiProvider, settingsDataStoreProvider);
  }

  public static DeviceDiscoveryManager newInstance(Context context, KodiApi kodiApi,
      SettingsDataStore settingsDataStore) {
    return new DeviceDiscoveryManager(context, kodiApi, settingsDataStore);
  }
}
