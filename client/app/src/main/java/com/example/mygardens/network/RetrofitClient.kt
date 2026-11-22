package com.example.mygardens.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton object for Retrofit initialization and API service instantiation.
 */
object RetrofitClient {

    val apiService: MissionsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConfig.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MissionsApiService::class.java)
    }
}
