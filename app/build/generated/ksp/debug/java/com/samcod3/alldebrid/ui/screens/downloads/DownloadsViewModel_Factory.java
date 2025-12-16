package com.samcod3.alldebrid.ui.screens.downloads;

import com.samcod3.alldebrid.data.repository.AllDebridRepository;
import com.samcod3.alldebrid.data.repository.DeviceRepository;
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
public final class DownloadsViewModel_Factory implements Factory<DownloadsViewModel> {
  private final Provider<AllDebridRepository> repositoryProvider;

  private final Provider<DeviceRepository> deviceRepositoryProvider;

  public DownloadsViewModel_Factory(Provider<AllDebridRepository> repositoryProvider,
      Provider<DeviceRepository> deviceRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.deviceRepositoryProvider = deviceRepositoryProvider;
  }

  @Override
  public DownloadsViewModel get() {
    return newInstance(repositoryProvider.get(), deviceRepositoryProvider.get());
  }

  public static DownloadsViewModel_Factory create(Provider<AllDebridRepository> repositoryProvider,
      Provider<DeviceRepository> deviceRepositoryProvider) {
    return new DownloadsViewModel_Factory(repositoryProvider, deviceRepositoryProvider);
  }

  public static DownloadsViewModel newInstance(AllDebridRepository repository,
      DeviceRepository deviceRepository) {
    return new DownloadsViewModel(repository, deviceRepository);
  }
}
