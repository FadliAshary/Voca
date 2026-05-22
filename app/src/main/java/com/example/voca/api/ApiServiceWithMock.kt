package com.example.voca.api

import android.os.Handler
import android.os.Looper
import com.example.voca.model.FinanceTip
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {

    // --- Login & Register ---

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name")