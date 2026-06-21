package com.example.voca.api

import com.example.voca.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── EVENT ENDPOINTS ─────────────────────────────────────────

    // GET semua event
    @GET("events.php")
    suspend fun getAllEvents(): Response<ApiResponse<List<EventApiModel>>>

    // GET event berdasarkan ID
    @GET("events.php")
    suspend fun getEventById(
        @Query("id") id: Int
    ): Response<ApiResponse<EventApiModel>>

    // POST tambah event baru
    @POST("events.php")
    suspend fun addEvent(
        @Body event: EventRequest
    ): Response<ApiResponse<Map<String, Int>>>

    // PUT perbarui event
    @PUT("events.php")
    suspend fun updateEvent(
        @Query("id") id: Int,
        @Body event: EventRequest
    ): Response<ApiResponse<Unit>>

    // DELETE hapus event
    @DELETE("events.php")
    suspend fun deleteEvent(
        @Query("id") id: Int
    ): Response<ApiResponse<Unit>>

    // ── AUTH ENDPOINTS ──────────────────────────────────────────

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Map<String, Any>>

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Map<String, Any>>

    // Suspend versions for Coroutines
    @POST("auth.php")
    suspend fun loginSuspend(
        @Query("action") action: String = "login",
        @Body request: LoginRequest
    ): Response<ApiResponse<UserApiModel>>

    @POST("auth.php")
    suspend fun registerSuspend(
        @Query("action") action: String = "register",
        @Body request: RegisterRequest
    ): Response<ApiResponse<Map<String, Int>>>

    @GET("tips.php")
    fun getTips(): Call<List<FinanceTip>>

    // ── TRANSACTION ENDPOINTS ───────────────────────────────────

    @FormUrlEncoded
    @POST("add_transaction.php")
    fun addTransaction(
        @Field("user_id") userId: Int,
        @Field("title") title: String,
        @Field("amount") amount: Double,
        @Field("type") type: String,
        @Field("category") category: String,
        @Field("date") date: String
    ): Call<Map<String, Any>>

    companion object {
        fun create(): ApiService = RetrofitClient.apiService
        fun getInstance(): ApiService = RetrofitClient.apiService
    }
}
