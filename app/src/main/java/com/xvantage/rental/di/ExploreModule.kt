package com.xvantage.rental.di

import com.xvantage.rental.data.remote.ExploreApiInterface
import com.xvantage.rental.data.source.ExploreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * NetworkModule.kt itself is NOT touched. We simply request the app's
 * existing Retrofit singleton here (Hilt already provides it) and build a
 * second interface off it - same base URL, same OkHttpClient, same
 * AuthInterceptor (Bearer token attached automatically), same Gson.
 */
@Module
@InstallIn(SingletonComponent::class)
object ExploreModule {

    @Provides
    @Singleton
    fun provideExploreApiInterface(retrofit: Retrofit): ExploreApiInterface {
        return retrofit.create(ExploreApiInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideExploreRepository(apiInterface: ExploreApiInterface): ExploreRepository {
        return ExploreRepository(apiInterface)
    }
}