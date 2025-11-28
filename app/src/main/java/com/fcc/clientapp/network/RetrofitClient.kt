package com.fcc.clientapp.network

import com.fcc.clientapp.network.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.fcc.clientapp.BuildConfig

object RetrofitClient {
    // ⚠️ CONFIRMA TU IP
    private const val BASE_URL =  BuildConfig.API_BASE_URL

    private val okHttpClient = OkHttpClient.Builder()
        // Aumentamos a 30s para dar tiempo a subir imágenes
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}