package com.example.voca.utils

import android.content.Context
import android.util.Log
import com.example.voca.api.ApiService
import com.example.voca.database.DatabaseHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SyncManager(private val context: Context) {
    private val db = DatabaseHelper(context)
    private val apiService = ApiService.getInstance()
    private val session = SessionManager(context)

    fun syncTransactions() {
        val userId = session.getUserId()
        if (userId == -1) return

        val unsynced = db.getUnsyncedTransactions(userId)
        if (unsynced.isEmpty()) return

        Log.d("SyncManager", "Found ${unsynced.size} unsynced transactions")

        for (t in unsynced) {
            val localId = t["id"] as Int
            val title = t["title"] as String
            val amount = t["amount"] as Double
            val type = t["type"] as String
            val category = t["category"] as String
            val dateLocal = t["date"] as String

            // Convert dateLocal (dd MMM yyyy) to dateServer (yyyy-MM-dd)
            val dateServer = try {
                val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = inputFormat.parse(dateLocal)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            apiService.addTransaction(userId, title, amount, type, category, dateServer)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            val remoteId = (body?.get("id") as? Double)?.toInt() ?: 0
                            db.markTransactionSynced(localId, remoteId)
                            Log.d("SyncManager", "Synced transaction $localId successfully")
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        Log.e("SyncManager", "Failed to sync transaction $localId: ${t.message}")
                    }
                })
        }
    }
}
