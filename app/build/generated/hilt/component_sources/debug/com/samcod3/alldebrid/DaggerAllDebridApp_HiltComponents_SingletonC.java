package com.samcod3.alldebrid;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.samcod3.alldebrid.data.api.AllDebridApi;
import com.samcod3.alldebrid.data.api.DashboardApi;
import com.samcod3.alldebrid.data.api.JackettApi;
import com.samcod3.alldebrid.data.api.KodiApi;
import com.samcod3.alldebrid.data.datastore.SettingsDataStore;
import com.samcod3.alldebrid.data.repository.AllDebridRepository;
import com.samcod3.alldebrid.data.repository.DeviceRepository;
import com.samcod3.alldebrid.data.repository.JackettRepository;
import com.samcod3.alldebrid.di.AppModule_ProvideAllDebridApiFactory;
import com.samcod3.alldebrid.di.AppModule_ProvideAllDebridRetrofitFactory;
import com.samcod3.alldebrid.di.AppModule_ProvideGenericRetrofitFactory;
import com.samcod3.alldebrid.di.AppModule_ProvideJackettApiFactory;
import com.samcod3.alldebrid.di.AppModule_ProvideKodiApiFactory;
import com.samcod3.alldebrid.di.AppModule_ProvideOkHttpClientFactory;
import com.samcod3.alldebrid.di.AppModule_ProvideSettingsDataStoreFactory;
import com.samcod3.alldebrid.discovery.DeviceDiscoveryManager;
import com.samcod3.alldebrid.ui.screens.devices.DevicesViewModel;
import com.samcod3.alldebrid.ui.screens.devices.DevicesViewModel_HiltModules;
import com.samcod3.alldebrid.ui.screens.downloads.DownloadsViewModel;
import com.samcod3.alldebrid.ui.screens.downloads.DownloadsViewModel_HiltModules;
import com.samcod3.alldebrid.ui.screens.login.ApiKeyManagerViewModel;
import com.samcod3.alldebrid.ui.screens.login.ApiKeyManagerViewModel_HiltModules;
import com.samcod3.alldebrid.ui.screens.search.SearchViewModel;
import com.samcod3.alldebrid.ui.screens.search.SearchViewModel_HiltModules;
import com.samcod3.alldebrid.ui.screens.settings.SettingsViewModel;
import com.samcod3.alldebrid.ui.screens.settings.SettingsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerAllDebridApp_HiltComponents_SingletonC {
  private DaggerAllDebridApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public AllDebridApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements AllDebridApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements AllDebridApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements AllDebridApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements AllDebridApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements AllDebridApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements AllDebridApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements AllDebridApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public AllDebridApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends AllDebridApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends AllDebridApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends AllDebridApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends AllDebridApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_login_ApiKeyManagerViewModel, ApiKeyManagerViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_devices_DevicesViewModel, DevicesViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_downloads_DownloadsViewModel, DownloadsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_search_SearchViewModel, SearchViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_samcod3_alldebrid_ui_screens_settings_SettingsViewModel = "com.samcod3.alldebrid.ui.screens.settings.SettingsViewModel";

      static String com_samcod3_alldebrid_ui_screens_search_SearchViewModel = "com.samcod3.alldebrid.ui.screens.search.SearchViewModel";

      static String com_samcod3_alldebrid_ui_screens_downloads_DownloadsViewModel = "com.samcod3.alldebrid.ui.screens.downloads.DownloadsViewModel";

      static String com_samcod3_alldebrid_ui_screens_devices_DevicesViewModel = "com.samcod3.alldebrid.ui.screens.devices.DevicesViewModel";

      static String com_samcod3_alldebrid_ui_screens_login_ApiKeyManagerViewModel = "com.samcod3.alldebrid.ui.screens.login.ApiKeyManagerViewModel";

      @KeepFieldType
      SettingsViewModel com_samcod3_alldebrid_ui_screens_settings_SettingsViewModel2;

      @KeepFieldType
      SearchViewModel com_samcod3_alldebrid_ui_screens_search_SearchViewModel2;

      @KeepFieldType
      DownloadsViewModel com_samcod3_alldebrid_ui_screens_downloads_DownloadsViewModel2;

      @KeepFieldType
      DevicesViewModel com_samcod3_alldebrid_ui_screens_devices_DevicesViewModel2;

      @KeepFieldType
      ApiKeyManagerViewModel com_samcod3_alldebrid_ui_screens_login_ApiKeyManagerViewModel2;
    }
  }

  private static final class ViewModelCImpl extends AllDebridApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<ApiKeyManagerViewModel> apiKeyManagerViewModelProvider;

    private Provider<DevicesViewModel> devicesViewModelProvider;

    private Provider<DownloadsViewModel> downloadsViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.apiKeyManagerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.devicesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.downloadsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_login_ApiKeyManagerViewModel, ((Provider) apiKeyManagerViewModelProvider)).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_devices_DevicesViewModel, ((Provider) devicesViewModelProvider)).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_downloads_DownloadsViewModel, ((Provider) downloadsViewModelProvider)).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_search_SearchViewModel, ((Provider) searchViewModelProvider)).put(LazyClassKeyProvider.com_samcod3_alldebrid_ui_screens_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_samcod3_alldebrid_ui_screens_search_SearchViewModel = "com.samcod3.alldebrid.ui.screens.search.SearchViewModel";

      static String com_samcod3_alldebrid_ui_screens_login_ApiKeyManagerViewModel = "com.samcod3.alldebrid.ui.screens.login.ApiKeyManagerViewModel";

      static String com_samcod3_alldebrid_ui_screens_downloads_DownloadsViewModel = "com.samcod3.alldebrid.ui.screens.downloads.DownloadsViewModel";

      static String com_samcod3_alldebrid_ui_screens_devices_DevicesViewModel = "com.samcod3.alldebrid.ui.screens.devices.DevicesViewModel";

      static String com_samcod3_alldebrid_ui_screens_settings_SettingsViewModel = "com.samcod3.alldebrid.ui.screens.settings.SettingsViewModel";

      @KeepFieldType
      SearchViewModel com_samcod3_alldebrid_ui_screens_search_SearchViewModel2;

      @KeepFieldType
      ApiKeyManagerViewModel com_samcod3_alldebrid_ui_screens_login_ApiKeyManagerViewModel2;

      @KeepFieldType
      DownloadsViewModel com_samcod3_alldebrid_ui_screens_downloads_DownloadsViewModel2;

      @KeepFieldType
      DevicesViewModel com_samcod3_alldebrid_ui_screens_devices_DevicesViewModel2;

      @KeepFieldType
      SettingsViewModel com_samcod3_alldebrid_ui_screens_settings_SettingsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.samcod3.alldebrid.ui.screens.login.ApiKeyManagerViewModel 
          return (T) new ApiKeyManagerViewModel(singletonCImpl.dashboardApiProvider.get(), singletonCImpl.provideSettingsDataStoreProvider.get());

          case 1: // com.samcod3.alldebrid.ui.screens.devices.DevicesViewModel 
          return (T) new DevicesViewModel(singletonCImpl.deviceRepositoryProvider.get());

          case 2: // com.samcod3.alldebrid.ui.screens.downloads.DownloadsViewModel 
          return (T) new DownloadsViewModel(singletonCImpl.allDebridRepositoryProvider.get(), singletonCImpl.deviceRepositoryProvider.get());

          case 3: // com.samcod3.alldebrid.ui.screens.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.jackettRepositoryProvider.get(), singletonCImpl.allDebridRepositoryProvider.get());

          case 4: // com.samcod3.alldebrid.ui.screens.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideSettingsDataStoreProvider.get(), singletonCImpl.allDebridRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends AllDebridApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends AllDebridApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends AllDebridApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<DashboardApi> dashboardApiProvider;

    private Provider<SettingsDataStore> provideSettingsDataStoreProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Retrofit> provideGenericRetrofitProvider;

    private Provider<KodiApi> provideKodiApiProvider;

    private Provider<DeviceDiscoveryManager> deviceDiscoveryManagerProvider;

    private Provider<DeviceRepository> deviceRepositoryProvider;

    private Provider<Retrofit> provideAllDebridRetrofitProvider;

    private Provider<AllDebridApi> provideAllDebridApiProvider;

    private Provider<AllDebridRepository> allDebridRepositoryProvider;

    private Provider<JackettApi> provideJackettApiProvider;

    private Provider<JackettRepository> jackettRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.dashboardApiProvider = DoubleCheck.provider(new SwitchingProvider<DashboardApi>(singletonCImpl, 0));
      this.provideSettingsDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<SettingsDataStore>(singletonCImpl, 1));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 5));
      this.provideGenericRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 4));
      this.provideKodiApiProvider = DoubleCheck.provider(new SwitchingProvider<KodiApi>(singletonCImpl, 3));
      this.deviceDiscoveryManagerProvider = DoubleCheck.provider(new SwitchingProvider<DeviceDiscoveryManager>(singletonCImpl, 6));
      this.deviceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DeviceRepository>(singletonCImpl, 2));
      this.provideAllDebridRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 9));
      this.provideAllDebridApiProvider = DoubleCheck.provider(new SwitchingProvider<AllDebridApi>(singletonCImpl, 8));
      this.allDebridRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AllDebridRepository>(singletonCImpl, 7));
      this.provideJackettApiProvider = DoubleCheck.provider(new SwitchingProvider<JackettApi>(singletonCImpl, 11));
      this.jackettRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<JackettRepository>(singletonCImpl, 10));
    }

    @Override
    public void injectAllDebridApp(AllDebridApp allDebridApp) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.samcod3.alldebrid.data.api.DashboardApi 
          return (T) new DashboardApi();

          case 1: // com.samcod3.alldebrid.data.datastore.SettingsDataStore 
          return (T) AppModule_ProvideSettingsDataStoreFactory.provideSettingsDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.samcod3.alldebrid.data.repository.DeviceRepository 
          return (T) new DeviceRepository(singletonCImpl.provideKodiApiProvider.get(), singletonCImpl.provideSettingsDataStoreProvider.get(), singletonCImpl.deviceDiscoveryManagerProvider.get());

          case 3: // com.samcod3.alldebrid.data.api.KodiApi 
          return (T) AppModule_ProvideKodiApiFactory.provideKodiApi(singletonCImpl.provideGenericRetrofitProvider.get());

          case 4: // @com.samcod3.alldebrid.di.GenericRetrofit retrofit2.Retrofit 
          return (T) AppModule_ProvideGenericRetrofitFactory.provideGenericRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 5: // okhttp3.OkHttpClient 
          return (T) AppModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 6: // com.samcod3.alldebrid.discovery.DeviceDiscoveryManager 
          return (T) new DeviceDiscoveryManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideKodiApiProvider.get(), singletonCImpl.provideSettingsDataStoreProvider.get());

          case 7: // com.samcod3.alldebrid.data.repository.AllDebridRepository 
          return (T) new AllDebridRepository(singletonCImpl.provideAllDebridApiProvider.get(), singletonCImpl.provideSettingsDataStoreProvider.get());

          case 8: // com.samcod3.alldebrid.data.api.AllDebridApi 
          return (T) AppModule_ProvideAllDebridApiFactory.provideAllDebridApi(singletonCImpl.provideAllDebridRetrofitProvider.get());

          case 9: // @com.samcod3.alldebrid.di.AllDebridRetrofit retrofit2.Retrofit 
          return (T) AppModule_ProvideAllDebridRetrofitFactory.provideAllDebridRetrofit(singletonCImpl.provideOkHttpClientProvider.get());

          case 10: // com.samcod3.alldebrid.data.repository.JackettRepository 
          return (T) new JackettRepository(singletonCImpl.provideJackettApiProvider.get(), singletonCImpl.provideSettingsDataStoreProvider.get());

          case 11: // com.samcod3.alldebrid.data.api.JackettApi 
          return (T) AppModule_ProvideJackettApiFactory.provideJackettApi(singletonCImpl.provideGenericRetrofitProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
