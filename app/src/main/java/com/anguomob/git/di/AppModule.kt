package com.anguomob.git.di

import android.content.Context
import com.anguomob.git.data.api.AuthInterceptor
import com.anguomob.git.data.api.GitHubApiService
import com.anguomob.git.data.api.OpenRouterApiService
import com.anguomob.git.data.local.GitRadarDatabase
import com.anguomob.git.data.repository.GitHubRepositoryImpl
import com.anguomob.git.domain.repository.GitHubRepository
import com.google.gson.Gson
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GitRadarDatabase {
        return GitRadarDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubApiService(okHttpClient: OkHttpClient): GitHubApiService {
        return Retrofit.Builder()
            .baseUrl(com.anguomob.git.util.Constants.GITHUB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(GitHubApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenRouterApiService(okHttpClient: OkHttpClient): OpenRouterApiService {
        return Retrofit.Builder()
            .baseUrl(com.anguomob.git.util.Constants.OPENROUTER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(OpenRouterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}
