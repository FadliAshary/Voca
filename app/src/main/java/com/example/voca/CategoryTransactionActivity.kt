package com.example.voca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityCategoryTransactionBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager

class CategoryTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryTransactionBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Lainnya"

        binding.tvToolbarTitle.text = "Transaksi: $categoryName"
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun loadTransactions() {
        val userId = session.getUserId()
        val allTransactions = db.getAllTransactions(userId)
        val filtered = allTransactions.filter { it["category"] == categoryName }

        val container = binding.containerTransactions
        container.removeAllViews()

        if (filtered.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "Belum ada transaksi di kategori ini"
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
                setTextColor(androidx.core.content.ContextCompat.getColor(this@CategoryTransactionActivity, R.color.voca_text_gray))
            }
            container.addView(emptyText)
            return
        }

        val currencyCode = session.getCurrency()
        filtered.forEach { t ->
            val itemView = layoutInflater.inflate(R.layout.item_transaction_simple, container, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTransTitle)
            val tvDate = itemView.findViewById<TextView>(R.id.tvTransDate)
            val tvAmount = itemView.findViewById<TextView>(R.id.tvTransAmount)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivTransIcon)

            val title = t["title"]?.toString() ?: "-"
            val amount = (t["amount"] as? Double) ?: 0.0
            val type = t["type"]?.toString() ?: "expense"
            val date = t["date"]?.toString() ?: "-"
            val id = (t["id"] as? Int) ?: -1

            tvTitle.text = title
            tvDate.text = date

            if (type == "income") {
                tvAmount.text = "+${CurrencyUtils.formatCurrency(amount, currencyCode)}"
                tvAmount.setTextColor(android.graphics.Color.parseColor("#48BB78"))
                ivIcon.setImageResource(R.drawable.ic_income_default)
                ivIcon.setBackgroundResource(R.drawable.circle_green_bg)
            } else {
                tvAmount.text = "-${CurrencyUtils.formatCurrency(amount, currencyCode)}"
                tvAmount.setTextColor(android.graphics.Color.parseColor("#F56565"))
                
                when (categoryName) {
                    "Makanan" -> ivIcon.setImageResource(R.drawable.ic_food)
                    "Transport" -> ivIcon.setImageResource(R.drawable.ic_transport)
                    "Belanja" -> ivIcon.setImageResource(R.drawable.ic_shopping)
                    "Tagihan" -> ivIcon.setImageResource(R.drawable.ic_bill)
                    else -> ivIcon.setImageResource(R.drawable.ic_more)
                }
                ivIcon.setBackgroundResource(R.drawable.circle_light_bg)
            }

            itemView.setOnClickListener {
                val intent = Intent(this, DetailTransactionActivity::class.java)
                intent.putExtra("TRANSACTION_ID", id)
                startActivity(intent)
            }

            container.addView(itemView)
        }
    }
}
