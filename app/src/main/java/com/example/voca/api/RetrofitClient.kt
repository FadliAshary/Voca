package com.example.voca.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val BASE_URL: String
        get() = com.example.voca.utils.AppConfig.SERVER_URL.let { 
            if (it.endsWith("/")) it else "$it/" 
        }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
