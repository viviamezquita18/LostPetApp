package com.app.lostpetapp.di

import com.app.lostpetapp.api.ApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const val BASE_URL = "http://192.168.0.5:4000/"

@Module
@InstallIn(ApplicationComponent::class)
object AppModule{

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder().setLenient().create()
    }

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson):Retrofit{
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).baseUrl(
            BASE_URL).build()
    }

    @Singleton
    @Provides
    fun providePetService(retrofit: Retrofit) = retrofit.create(ApiService::class.java)




}