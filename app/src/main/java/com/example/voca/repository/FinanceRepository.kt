package com.example.voca.repository

import com.example.voca.api.ApiService
import com.example.voca.model.FinanceTip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FinanceRepository(private val apiService: ApiService) {

    fun getFinanceTips(callback: (List<FinanceTip>?, Throwable?) -> Unit) {
        apiService.getTips().enqueue(object : Callback<List<FinanceTip>> {
            override fun onResponse(call: Call<List<FinanceTip>>, response: Response<List<FinanceTip>>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, Exception("Error: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<List<FinanceTip>>, t: Throwable) {
                callback(null, t)
            }
        })
    }
}