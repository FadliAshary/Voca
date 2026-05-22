package com.example.voca

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityDetailTransactionBinding
import java.text.NumberFormat
import java.util.*

class DetailTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransactionBinding
    private lateinit var db: DatabaseHelper
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        if (transactionId != -1) {
            loadTransactionDetails()
        } else {
            Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.btnDelete.setOnClickListener {
            if (db.deleteTransaction(transactionId) > 0) {
                Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadTransactionDetails() {
        val t = db.getTransactionById(transactionId)
        if (t != null) {
            val title = t["title"] as String
            val amount = t["amount"] as Double
            val category = t["category"] as? String ?: "Lainnya"
            val date = t["date"] as String

            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvDetailAmount.text = formatter.format(amount).replace("Rp", "Rp ")
            binding.tvDetailMerchant.text = title
            binding.tvDetailCategory.text = category
            binding.tvDetailDate.text = date

            // Set icon based on category
            when (category) {
                "Makanan" -> binding.ivDetailIcon.setImageResource(R.drawable.ic_food)
                "Transport" -> binding.ivDetailIcon.setImageResource(R.drawable.ic_transport)
                "Belanja" -> binding.ivDetailIcon.setImageResource(R.drawable.ic_shopping)
                else -> binding.ivDetailIcon.setImageResource(R.drawable.ic_more)
            }
        }
    }
}