package com.arya.sapworkpilot.di

import com.arya.sapworkpilot.data.remote.AuditApi
import com.arya.sapworkpilot.data.remote.AuthApi
import com.arya.sapworkpilot.data.remote.AuthInterceptor
import com.arya.sapworkpilot.data.remote.ChatApi
import com.arya.sapworkpilot.data.remote.DocumentApi
import com.arya.sapworkpilot.data.remote.DynamicBaseUrlInterceptor
import com.arya.sapworkpilot.data.remote.HealthApi
import com.arya.sapworkpilot.data.remote.MeetingApi
import com.arya.sapworkpilot.data.remote.ProjectApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // This is only a placeholder now, since DynamicBaseUrlInterceptor rewrites
    // the host per request based on what's saved in Settings.
    private const val PLACEHOLDER_URL = "http://192.168.10.51:8000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(PLACEHOLDER_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProjectApi(retrofit: Retrofit): ProjectApi = retrofit.create(ProjectApi::class.java)

    @Provides
    @Singleton
    fun provideChatApi(retrofit: Retrofit): ChatApi = retrofit.create(ChatApi::class.java)

    @Provides
    @Singleton
    fun provideDocumentApi(retrofit: Retrofit): DocumentApi = retrofit.create(DocumentApi::class.java)

    @Provides
    @Singleton
    fun provideMeetingApi(retrofit: Retrofit): MeetingApi = retrofit.create(MeetingApi::class.java)

    @Provides
    @Singleton
    fun provideAuditApi(retrofit: Retrofit): AuditApi = retrofit.create(AuditApi::class.java)

    @Provides
    @Singleton
    fun provideHealthApi(retrofit: Retrofit): HealthApi = retrofit.create(HealthApi::class.java)
}