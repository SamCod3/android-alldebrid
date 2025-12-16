package com.samcod3.alldebrid.ui.screens.search;

import com.samcod3.alldebrid.data.repository.AllDebridRepository;
import com.samcod3.alldebrid.data.repository.JackettRepository;
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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<JackettRepository> jackettRepositoryProvider;

  private final Provider<AllDebridRepository> allDebridRepositoryProvider;

  public SearchViewModel_Factory(Provider<JackettRepository> jackettRepositoryProvider,
      Provider<AllDebridRepository> allDebridRepositoryProvider) {
    this.jackettRepositoryProvider = jackettRepositoryProvider;
    this.allDebridRepositoryProvider = allDebridRepositoryProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(jackettRepositoryProvider.get(), allDebridRepositoryProvider.get());
  }

  public static SearchViewModel_Factory create(
      Provider<JackettRepository> jackettRepositoryProvider,
      Provider<AllDebridRepository> allDebridRepositoryProvider) {
    return new SearchViewModel_Factory(jackettRepositoryProvider, allDebridRepositoryProvider);
  }

  public static SearchViewModel newInstance(JackettRepository jackettRepository,
      AllDebridRepository allDebridRepository) {
    return new SearchViewModel(jackettRepository, allDebridRepository);
  }
}
