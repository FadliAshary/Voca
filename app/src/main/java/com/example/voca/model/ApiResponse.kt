package com.example.voca.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data")    val data: T?
)

// Model data Event dari API (sesuai kolom tabel MySQL)
data class EventApiModel(
    @SerializedName("id")          val id: Int,
    @SerializedName("name")        val name: String,
    @SerializedName("date")        val date: String,
    @SerializedName("location")    val location: String,
    @SerializedName("price")       val price: Int,
    @SerializedName("description") val description: String?
) {
    // Konversi dari model API ke model lokal
    fun toEvent(): Event = Event(
        id       = id,
        name     = name,
        date     = date,
        location = location,
        price    = price
    )
}

// Model data User dari API
data class UserApiModel(
    @SerializedName("id")       val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email")    val email: String,
    @SerializedName("phone")    val phone: String?
)

// Request body untuk Login & Register
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email")    val email: String,
    @SerializedName("phone")    val phone: String = ""
)

// Request body untuk Event baru
data class EventRequest(
    @SerializedName("name")        val name: String,
    @SerializedName("date")        val date: String,
    @SerializedName("location")    val location: String,
    @SerializedName("price")       val price: Int,
    @SerializedName("description") val description: String = ""
)
