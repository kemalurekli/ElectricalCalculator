package com.kemalurekli.electricalcalculator.core.data.network.di

import com.kemalurekli.electricalcalculator.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * HTTP and serialization infrastructure.
 *
 * ElecToolkit is offline-first and ships no network calls today. This module
 * exists so the planned remote modules — standards, cable, transformer and
 * motor databases — can be added without revisiting DI.
 *
 * Note that no `Retrofit` instance is provided: a Retrofit needs a base URL,
 * and inventing one before an API exists would be a fiction the compiler cannot
 * check. A [Retrofit.Builder] is provided instead, pre-wired with the shared
 * client and converter, so a future feature supplies only its own base URL.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Tolerate fields added by a newer server without shipping an app update.
        ignoreUnknownKeys = true
        // Absent fields fall back to the Kotlin default rather than failing.
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // Bodies are logged only in debug; release builds stay silent so
                // no response content can reach logcat on a user's device.
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            },
        )
        .build()

    @Provides
    @Singleton
    fun provideRetrofitBuilder(
        client: OkHttpClient,
        json: Json,
    ): Retrofit.Builder = Retrofit.Builder()
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
}
