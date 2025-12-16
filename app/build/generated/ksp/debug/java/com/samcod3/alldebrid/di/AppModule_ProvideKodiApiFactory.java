package com.samcod3.alldebrid.di;

import com.samcod3.alldebrid.data.api.KodiApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.samcod3.alldebrid.di.GenericRetrofit")
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
public final class AppModule_ProvideKodiApiFactory implements Factory<KodiApi> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideKodiApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public KodiApi get() {
    return provideKodiApi(retrofitProvider.get());
  }

  public static AppModule_ProvideKodiApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideKodiApiFactory(retrofitProvider);
  }

  public static KodiApi provideKodiApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideKodiApi(retrofit));
  }
}
