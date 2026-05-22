package com.example.voca.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Ganti dengan IP server masing-masing:
    // - Emulator Android Studio : http://10.0.2.2/event_api/
    // - HP fisik (WiFi sama)    : http://192.168.x.x/event_api/
    // - Hosting online          : https://yourdomain.com/event_api/
    private const val BASE_URL = "http://10.61.56.98/voca_api" // Adjusted to project context if needed, but guide says 10.0.2.2/event_api/

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
