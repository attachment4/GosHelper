package com.gospomoshnik.di

import com.gospomoshnik.BuildConfig
import com.gospomoshnik.data.remote.GigaChatApi
import com.gospomoshnik.data.remote.GigaChatOAuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GIGACHAT_BASE_URL = "https://gigachat.devices.sberbank.ru/api/v1/"
    private const val OAUTH_BASE_URL    = "https://ngw.devices.sberbank.ru:9443/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideGigaChatApi(client: OkHttpClient): GigaChatApi =
        Retrofit.Builder()
            .baseUrl(GIGACHAT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GigaChatApi::class.java)

    @Provides
    @Singleton
    fun provideGigaChatOAuthApi(client: OkHttpClient): GigaChatOAuthApi =
        Retrofit.Builder()
            .baseUrl(OAUTH_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GigaChatOAuthApi::class.java)

    @Provides
    @Named("gigachat_auth")
    fun provideGigaChatAuth(): String = BuildConfig.GIGACHAT_AUTH
}
