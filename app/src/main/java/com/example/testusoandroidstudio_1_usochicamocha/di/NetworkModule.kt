package com.example.testusoandroidstudio_1_usochicamocha.di

import android.util.Log
import com.example.testusoandroidstudio_1_usochicamocha.BuildConfig
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.ApiService
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.AuthInterceptor
import com.example.testusoandroidstudio_1_usochicamocha.data.remote.TokenAuthenticator
import com.example.testusoandroidstudio_1_usochicamocha.util.redactarSecretos
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(tokenAuthenticator: TokenAuthenticator, authInterceptor: AuthInterceptor): ApiService {
        // En release no se loguea nada de la red; en debug, sin contraseña ni tokens.
        val loggingInterceptor = HttpLoggingInterceptor { mensaje ->
            Log.i("okhttp.OkHttpClient", redactarSecretos(mensaje))
        }.apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
