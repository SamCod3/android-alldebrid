package com.samcod3.alldebrid.di

import android.content.Context
import com.samcod3.alldebrid.data.api.AllDebridApi
import com.samcod3.alldebrid.data.api.JackettApi
import com.samcod3.alldebrid.data.api.KodiApi
import com.samcod3.alldebrid.data.datastore.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AllDebridRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GenericRetrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @AllDebridRetrofit
    fun provideAllDebridRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AllDebridApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @GenericRetrofit
    fun provideGenericRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/") // Placeholder, actual URL set per request
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAllDebridApi(@AllDebridRetrofit retrofit: Retrofit): AllDebridApi {
        return retrofit.create(AllDebridApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideJackettApi(@GenericRetrofit retrofit: Retrofit): JackettApi {
        return retrofit.create(JackettApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideKodiApi(@GenericRetrofit retrofit: Retrofit): KodiApi {
        return retrofit.create(KodiApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
