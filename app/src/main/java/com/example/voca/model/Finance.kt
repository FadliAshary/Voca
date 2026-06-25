package com.example.voca.model

data class Finance(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String,
    val date: String,
    val imagePath: String? = null,
    val isSynced: Int = 0,
    val remoteId: Int = 0
)
