package com.example.voca

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityAiInsightsBinding
import java.text.NumberFormat
import java.util.*

class AiInsightsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiInsightsBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        analyzeData()
    }

    private fun analyzeData() {
        val transactions = db.getAllTransactions()
        val goals = db.getAllGoals()

        var totalIncome = 0.0
        var totalExpense = 0.0
        val categoryTotals = mutableMapOf<String, Double>()

        for (t in transactions) {
            val amount = t["amount"] as Double
            if (t["type"] == "income") {
                totalIncome += amount
            } else {
                totalExpense += amount
                val cat = t["category"] as? String ?: "Lainnya"
                categoryTotals[cat] = (categoryTotals[cat] ?: 0.0) + amount
            }
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // 1. Predictive Logic
        val prediction = if (totalExpense > totalIncome && totalIncome > 0) {
            "Peringatan: Pengeluaran Anda melebihi pemasukan. Tren ini dapat menguras tabungan Anda dalam waktu dekat."
        } else if (totalExpense > 0) {
            "Bagus! Pengeluaran Anda terkontrol. Anda mengalokasikan sekitar ${((totalIncome - totalExpense) / totalIncome * 100).toInt()}% pemasukan ke tabungan."
        } else {
            "Belum ada data pengeluaran yang cukup untuk melakukan prediksi bulan depan."
        }
        binding.tvAiPrediction.text = prediction

        // 2. Saving Tips Logic
        val topCategory = categoryTotals.maxByOrNull { it.value }
        val savingTip = if (topCategory != null) {
            "Pengeluaran terbesar Anda ada di kategori ${topCategory.key} (${formatter.format(topCategory.value).replace("Rp", "Rp ")}). Cobalah kurangi 10% di kategori ini untuk menambah saldo tabungan."
        } else {
            "Mulailah mencatat transaksi harian Anda untuk mendapatkan tips penghematan yang lebih akurat."
        }
        binding.tvAiSavingTip.text = savingTip

        // 3. Goal Milestone Logic
        val goalTip = if (goals.isNotEmpty()) {
            val firstGoal = goals[0]
            val name = firstGoal["name"] as String
            val target = firstGoal["target_amount"] as Double
            val current = firstGoal["current_amount"] as Double
            val remaining = target - current
            
            if (remaining <= 0) {
                "Selamat! Target '$name' Anda sudah tercapai."
            } else {
                "Untuk target '$name', Anda membutuhkan ${formatter.format(remaining).replace("Rp", "Rp ")} lagi. Tetap konsisten!"
            }
        } else {
            "Anda belum membuat Tabungan Impian. Buat satu sekarang untuk mulai merencanakan masa depan."
        }
        binding.tvAiGoalStatus.text = goalTip
    }
}