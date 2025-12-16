package com.samcod3.alldebrid.di;

import com.samcod3.alldebrid.data.api.AllDebridApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.samcod3.alldebrid.di.AllDebridRetrofit")
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
public final class AppModule_ProvideAllDebridApiFactory implements Factory<AllDebridApi> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideAllDebridApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public AllDebridApi get() {
    return provideAllDebridApi(retrofitProvider.get());
  }

  public static AppModule_ProvideAllDebridApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideAllDebridApiFactory(retrofitProvider);
  }

  public static AllDebridApi provideAllDebridApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAllDebridApi(retrofit));
  }
}
