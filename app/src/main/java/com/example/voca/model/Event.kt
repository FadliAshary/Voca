package com.example.voca.model

data class Event(
    val id: Int = 0,
    val name: String,
    val date: String,
    val location: String,
    val price: Int,
    val isRegistered: Boolean = false
) {
    fun getFormattedPrice(): String = "Rp $price"
}
