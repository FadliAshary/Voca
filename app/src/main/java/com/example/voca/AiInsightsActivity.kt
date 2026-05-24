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
    private lateinit var session: com.example.voca.utils.SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiInsightsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = com.example.voca.utils.SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        analyzeData()
    }

    private fun analyzeData() {
        val userId = session.getUserId()
        val transactions = db.getAllTransactions(userId)
        val goals = db.getAllGoals(userId)

        var totalIncome = 0.0
        var totalExpense = 0.0
        val categoryTotals = mutableMapOf<String, Double>()
        
        // Data mingguan untuk tren
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        var currentMonthExpense = 0.0

        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        for (t in transactions) {
            val amount = (t["amount"] as? Double) ?: 0.0
            val type = t["type"]?.toString() ?: "expense"
            val dateStr = t["date"]?.toString() ?: ""
            
            if (type == "income") {
                totalIncome += amount
            } else {
                totalExpense += amount
                val cat = t["category"]?.toString() ?: "Lainnya"
                categoryTotals[cat] = (categoryTotals[cat] ?: 0.0) + amount
                
                try {
                    val date = sdf.parse(dateStr)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        if (cal.get(Calendar.MONTH) == currentMonth) {
                            currentMonthExpense += amount
                        }
                    }
                } catch (e: Exception) {}
            }
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val netWorth = totalIncome - totalExpense

        // 1. Predictive Logic (Advanced)
        val prediction: String
        val dailyBudget = if (totalIncome > 0) (totalIncome / 30) else 0.0
        val avgDailyExpense = if (transactions.isNotEmpty()) totalExpense / 30 else 0.0

        prediction = when {
            netWorth < 0 -> {
                "⚠️ Darurat Keuangan: Saldo Anda minus ${formatter.format(Math.abs(netWorth))}. Prediksi AI: Jika pola ini berlanjut, Anda akan mengalami kesulitan finansial dalam 7 hari ke depan. Segera hentikan pengeluaran non-esensial."
            }
            avgDailyExpense > dailyBudget && totalIncome > 0 -> {
                "📉 Tren Negatif: Pengeluaran harian Anda (${formatter.format(avgDailyExpense)}) melebihi batas aman harian (${formatter.format(dailyBudget)}). Prediksi: Saldo Anda akan habis dalam waktu sekitar ${(netWorth / avgDailyExpense).toInt()} hari."
            }
            totalIncome > 0 && totalExpense > 0 -> {
                val savingsRate = (netWorth / totalIncome) * 100
                "📈 Tren Positif: Anda menabung ${String.format(Locale.getDefault(), "%.1f", savingsRate)}% dari pendapatan. Prediksi: Dalam 6 bulan, kekayaan bersih Anda diprediksi mencapai ${formatter.format(netWorth + (netWorth * 6))}. Pertahankan!"
            }
            else -> "Belum ada data yang cukup untuk analisis AI. Terus catat transaksi Anda selama 30 hari."
        }
        binding.tvAiPrediction.text = prediction

        // 2. Saving Tips Logic (Category Based)
        val topCategory = categoryTotals.maxByOrNull { it.value }
        val savingTip = if (topCategory != null && totalExpense > 0) {
            val percentage = (topCategory.value / totalExpense) * 100
            "💡 Tips Hemat: Kategori '${topCategory.key}' menghabiskan ${String.format(Locale.getDefault(), "%.1f", percentage)}% total pengeluaran Anda. AI menyarankan untuk beralih ke alternatif yang lebih murah untuk kategori ini guna menambah tabungan sebesar ${formatter.format(topCategory.value * 0.2)} per bulan."
        } else {
            "Gunakan kategori saat mencatat transaksi agar AI bisa memberikan saran penghematan yang spesifik."
        }
        binding.tvAiSavingTip.text = savingTip

        // 3. Goal Milestone Logic
        val goalTip = if (goals.isNotEmpty()) {
            val firstGoal = goals[0]
            val name = firstGoal["name"]?.toString() ?: "Target"
            val target = (firstGoal["target_amount"] as? Double) ?: 0.0
            val current = (firstGoal["current_amount"] as? Double) ?: 0.0
            val remaining = target - current
            
            if (remaining <= 0) {
                "🥳 Luar Biasa! Target '$name' telah tercapai. AI menyarankan untuk memindahkan alokasi dana ini ke investasi jangka panjang."
            } else {
                val monthsToGoal = if (netWorth > 0) remaining / (netWorth / 2) else Double.POSITIVE_INFINITY
                if (monthsToGoal < 100) {
                    "🎯 Fokus Target: Dengan sisa ${formatter.format(remaining)}, Anda diprediksi mencapai '$name' dalam ${String.format(Locale.getDefault(), "%.1f", monthsToGoal)} bulan jika konsisten menabung setengah dari surplus bulanan."
                } else {
                    "🎯 Fokus Target: Target '$name' masih jauh. AI menyarankan peningkatan pendapatan atau pengurangan biaya hidup sebesar 15% untuk mempercepat pencapaian."
                }
            }
        } else {
            "Anda belum memiliki target tabungan. Orang dengan target yang jelas cenderung menabung 2x lebih banyak."
        }
        binding.tvAiGoalStatus.text = goalTip
    }
}