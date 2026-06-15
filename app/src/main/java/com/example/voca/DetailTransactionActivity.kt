package com.example.voca

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityDetailTransactionBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetailTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransactionBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var transactionId: Int = -1
    private var transactionType: String = "expense"
    private var transactionCategory: String = "Lainnya"
    private var transactionDate: String = ""
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)
        transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        if (transactionId != -1) {
            loadTransactionDetails()
        } else {
            Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        
        setupEditListeners()

        binding.btnDelete.setOnClickListener {
            if (db.deleteTransaction(transactionId) > 0) {
                Toast.makeText(this, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupEditListeners() {
        binding.rowDetailType.setOnClickListener {
            val types = arrayOf("Pengeluaran", "Pemasukan")
            AlertDialog.Builder(this)
                .setTitle("Pilih Tipe")
                .setItems(types) { _, which ->
                    transactionType = if (which == 0) "expense" else "income"
                    updateTypeUI()
                }
                .show()
        }

        binding.rowDetailCategory.setOnClickListener {
            val categories = arrayOf("Makanan", "Transport", "Belanja", "Lainnya")
            AlertDialog.Builder(this)
                .setTitle("Pilih Kategori")
                .setItems(categories) { _, which ->
                    transactionCategory = categories[which]
                    binding.tvDetailCategory.text = transactionCategory
                    updateCategoryIcon(transactionCategory)
                }
                .show()
        }

        binding.rowDetailDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    transactionDate = dateFormat.format(calendar.time)
                    binding.tvDetailDate.text = transactionDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnChangeCategory.setOnClickListener {
            binding.rowDetailCategory.performClick()
        }
    }

    private fun updateCategoryIcon(category: String) {
        if (transactionType == "income") {
            binding.ivDetailIcon.setImageResource(R.drawable.ic_income_default)
            return
        }
        
        when (category) {
            "Makanan" -> binding.ivDetailIcon.setImageResource(R.drawable.ic_food)
            "Transport" -> binding.ivDetailIcon.setImageResource(R.drawable.ic_transport)
            "Belanja" -> binding.ivDetailIcon.setImageResource(R.drawable.ic_shopping)
            else -> binding.ivDetailIcon.setImageResource(R.drawable.ic_more)
        }
    }

    private fun updateTypeUI() {
        if (transactionType == "income") {
            binding.tvDetailType.text = "Pemasukan"
            binding.tvDetailType.setTextColor(android.graphics.Color.parseColor("#48BB78"))
            binding.tvDetailType.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E6FFFA"))
            binding.ivDetailIcon.setImageResource(R.drawable.ic_income_default)
        } else {
            binding.tvDetailType.text = "Pengeluaran"
            binding.tvDetailType.setTextColor(android.graphics.Color.parseColor("#3B82F6"))
            binding.tvDetailType.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E2F2FF"))
            updateCategoryIcon(transactionCategory)
        }
    }

    private fun saveChanges() {
        val title = binding.etDetailMerchant.text.toString()
        val amountStr = binding.etDetailAmount.text.toString()
            .replace("Rp", "")
            .replace(".", "")
            .replace(",", ".")
        val amount = amountStr.toDoubleOrNull() ?: 0.0

        if (db.updateTransaction(transactionId, title, amount, transactionType, transactionCategory, transactionDate) > 0) {
            Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTransactionDetails() {
        val t = db.getTransactionById(transactionId)
        if (t != null) {
            val title = t["title"] as String
            val amount = t["amount"] as Double
            transactionCategory = t["category"] as? String ?: "Lainnya"
            transactionDate = t["date"] as String
            transactionType = t["type"] as? String ?: "expense"
            val imagePath = t["image_path"] as? String

            // Update calendar for DatePicker initial value
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                dateFormat.parse(transactionDate)?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val currencyCode = session.getCurrency()
            binding.etDetailAmount.setText(CurrencyUtils.formatCurrency(amount, currencyCode).replace(currencyCode, "").trim())
            binding.etDetailMerchant.setText(title)
            binding.tvDetailCategory.text = transactionCategory
            binding.tvDetailDate.text = transactionDate
            updateTypeUI()

            // Handle image
            if (!imagePath.isNullOrEmpty()) {
                val imgFile = File(imagePath)
                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    binding.ivDetailReceipt.setImageBitmap(myBitmap)
                    binding.ivDetailReceipt.alpha = 1.0f
                    binding.layoutNoReceipt.visibility = View.GONE
                }
            }

            // Set icon based on category
            updateCategoryIcon(transactionCategory)
        }
    }
}