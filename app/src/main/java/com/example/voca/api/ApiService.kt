package com.example.voca.api

import com.example.voca.model.FinanceTip
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface ApiService {

    // ...existing code...

    companion object {
        // Try multiple URLs
        private val URLS = listOf(
            "http://10.0.2.2/voca_db/",      // Android Emulator
            "http://localhost/voca_db/",      // Local PC
            "http://192.168.1.7/voca_db/"     // Network PC
        )

        private var BASE_URL = URLS[0]  // Default ke emulator

        fun create(): ApiService {
            // Setup OkHttpClient dengan timeout
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build()
            return retrofit.create(ApiService::class.java)
        }

        fun setBaseUrl(url: String) {
            BASE_URL = url
        }

        fun getAvailableUrls(): List<String> = URLS
    }
}