package com.samcod3.alldebrid.di;

import android.content.Context;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideSettingsDataStoreFactory implements Factory<SettingsDataStore> {
  private final Provider<Context> contextProvider;

  public AppModule_ProvideSettingsDataStoreFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsDataStore get() {
    return provideSettingsDataStore(contextProvider.get());
  }

  public static AppModule_ProvideSettingsDataStoreFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideSettingsDataStoreFactory(contextProvider);
  }

  public static SettingsDataStore provideSettingsDataStore(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSettingsDataStore(context));
  }
}
