package com.samcod3.alldebrid.ui.screens.downloads;

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
public final class DownloadsViewModel_Factory implements Factory<DownloadsViewModel> {
  private final Provider<AllDebridRepository> repositoryProvider;

  public DownloadsViewModel_Factory(Provider<AllDebridRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DownloadsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static DownloadsViewModel_Factory create(
      Provider<AllDebridRepository> repositoryProvider) {
    return new DownloadsViewModel_Factory(repositoryProvider);
  }

  public static DownloadsViewModel newInstance(AllDebridRepository repository) {
    return new DownloadsViewModel(repository);
  }
}
