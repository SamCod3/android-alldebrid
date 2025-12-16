package com.samcod3.alldebrid.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class DlnaQueueManager_Factory implements Factory<DlnaQueueManager> {
  @Override
  public DlnaQueueManager get() {
    return newInstance();
  }

  public static DlnaQueueManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DlnaQueueManager newInstance() {
    return new DlnaQueueManager();
  }

  private static final class InstanceHolder {
    private static final DlnaQueueManager_Factory INSTANCE = new DlnaQueueManager_Factory();
  }
}
