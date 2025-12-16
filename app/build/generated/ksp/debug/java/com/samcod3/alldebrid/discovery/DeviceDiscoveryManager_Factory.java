package com.samcod3.alldebrid.discovery;

import android.content.Context;
import com.samcod3.alldebrid.data.api.KodiApi;
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

  public DeviceDiscoveryManager_Factory(Provider<Context> contextProvider,
      Provider<KodiApi> kodiApiProvider) {
    this.contextProvider = contextProvider;
    this.kodiApiProvider = kodiApiProvider;
  }

  @Override
  public DeviceDiscoveryManager get() {
    return newInstance(contextProvider.get(), kodiApiProvider.get());
  }

  public static DeviceDiscoveryManager_Factory create(Provider<Context> contextProvider,
      Provider<KodiApi> kodiApiProvider) {
    return new DeviceDiscoveryManager_Factory(contextProvider, kodiApiProvider);
  }

  public static DeviceDiscoveryManager newInstance(Context context, KodiApi kodiApi) {
    return new DeviceDiscoveryManager(context, kodiApi);
  }
}
