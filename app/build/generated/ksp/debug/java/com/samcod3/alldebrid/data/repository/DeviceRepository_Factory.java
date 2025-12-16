package com.samcod3.alldebrid.data.repository;

import com.samcod3.alldebrid.data.api.KodiApi;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import com.samcod3.alldebrid.discovery.DeviceDiscoveryManager;
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
public final class DeviceRepository_Factory implements Factory<DeviceRepository> {
  private final Provider<KodiApi> kodiApiProvider;

  private final Provider<SettingsDataStore> settingsDataStoreProvider;

  private final Provider<DeviceDiscoveryManager> discoveryManagerProvider;

  private final Provider<DlnaQueueManager> dlnaQueueProvider;

  public DeviceRepository_Factory(Provider<KodiApi> kodiApiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<DeviceDiscoveryManager> discoveryManagerProvider,
      Provider<DlnaQueueManager> dlnaQueueProvider) {
    this.kodiApiProvider = kodiApiProvider;
    this.settingsDataStoreProvider = settingsDataStoreProvider;
    this.discoveryManagerProvider = discoveryManagerProvider;
    this.dlnaQueueProvider = dlnaQueueProvider;
  }

  @Override
  public DeviceRepository get() {
    return newInstance(kodiApiProvider.get(), settingsDataStoreProvider.get(), discoveryManagerProvider.get(), dlnaQueueProvider.get());
  }

  public static DeviceRepository_Factory create(Provider<KodiApi> kodiApiProvider,
      Provider<SettingsDataStore> settingsDataStoreProvider,
      Provider<DeviceDiscoveryManager> discoveryManagerProvider,
      Provider<DlnaQueueManager> dlnaQueueProvider) {
    return new DeviceRepository_Factory(kodiApiProvider, settingsDataStoreProvider, discoveryManagerProvider, dlnaQueueProvider);
  }

  public static DeviceRepository newInstance(KodiApi kodiApi, SettingsDataStore settingsDataStore,
      DeviceDiscoveryManager discoveryManager, DlnaQueueManager dlnaQueue) {
    return new DeviceRepository(kodiApi, settingsDataStore, discoveryManager, dlnaQueue);
  }
}
