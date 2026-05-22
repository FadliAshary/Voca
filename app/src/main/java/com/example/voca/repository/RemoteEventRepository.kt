package com.example.voca.repository

import com.example.voca.api.RetrofitClient
import com.example.voca.model.Event
import com.example.voca.model.EventRequest

class RemoteEventRepository {

    private val api = RetrofitClient.apiService

    // Ambil semua event dari API
    suspend fun getAllEvents(): List<Event> {
        val response = api.getAllEvents()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true) {
                return body.data?.map { it.toEvent() } ?: emptyList()
            }
            throw Exception(body?.message ?: "Gagal mengambil data event")
        }
        throw Exception("HTTP Error: ${response.code()} ${response.message()}")
    }

    // Tambah event baru ke API
    suspend fun addEvent(event: Event): Boolean {
        val request  = EventRequest(event.name, event.date, event.location, event.price)
        val response = api.addEvent(request)
        if (response.isSuccessful) {
            return response.body()?.success == true
        }
        throw Exception("Gagal menambahkan event: ${response.message()}")
    }

    // Hapus event dari API
    suspend fun deleteEvent(id: Int): Boolean {
        val response = api.deleteEvent(id)
        if (response.isSuccessful) {
            return response.body()?.success == true
        }
        throw Exception("Gagal menghapus event")
    }

    // Update event di API
    suspend fun updateEvent(event: Event): Boolean {
        val request  = EventRequest(event.name, event.date, event.location, event.price)
        val response = api.updateEvent(event.id, request)
        if (response.isSuccessful) {
            return response.body()?.success == true
        }
        throw Exception("Gagal mengupdate event")
    }
}
