package com.example.voca.api

import com.example.voca.model.*
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

    // POST login
    @POST("auth.php")
    suspend fun login(
        @Query("action") action: String = "login",
        @Body request: LoginRequest
    ): Response<ApiResponse<UserApiModel>>

    // POST register
    @POST("auth.php")
    suspend fun register(
        @Query("action") action: String = "register",
        @Body request: RegisterRequest
    ): Response<ApiResponse<Map<String, Int>>>

    // Placeholder for compatibility if needed
    // @GET("tips.php")
    // suspend fun getTips(): Response<List<FinanceTip>>
}
