package com.example.voca

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.voca.api.ApiService
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityAddTransactionBinding
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: com.example.voca.utils.SessionManager
    private var selectedCategory = "Makanan"
    private var calendar = Calendar.getInstance()
    private var selectedAccount = "Tabungan Utama"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = com.example.voca.utils.SessionManager(this)

        setupToolbar()
        setupCategorySelection()
        setupDatePicker()
        setupAccountPicker()
        setupUploadReceipt()
        
        updateDateDisplay()

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID"))
        binding.tvDate.text = dateFormat.format(calendar.time)
    }

    private fun setupAccountPicker() {
        val accounts = arrayOf("Tabungan Utama", "Dompet Digital", "Kartu Kredit", "Investasi")
        binding.btnSelectAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Pilih Akun")
                .setItems(accounts) { _, which ->
                    selectedAccount = accounts[which]
                    binding.tvSelectedAccount.text = selectedAccount
                }
                .show()
        }
    }

    private fun setupUploadReceipt() {
        binding.btnUploadReceipt.setOnClickListener {
            // Mock functionality for receipt upload
            Toast.makeText(this, "Fitur Unggah Struk akan segera hadir!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategorySelection() {
        binding.catFood.setOnClickListener { selectCategory("Makanan") }
        binding.catTransport.setOnClickListener { selectCategory("Transport") }
        binding.catShopping.setOnClickListener { selectCategory("Belanja") }
        binding.catMore.setOnClickListener { selectCategory("Lainnya") }
        
        // Initial selection
        selectCategory("Makanan")
    }

    private fun selectCategory(name: String) {
        selectedCategory = name
        
        val categoryViews = mapOf(
            "Makanan" to Triple(binding.cardFood, binding.cardFood.getChildAt(0) as ImageView, "#E2F2FF"),
            "Transport" to Triple(binding.cardTransport, binding.cardTransport.getChildAt(0) as ImageView, "#F1F5F9"),
            "Belanja" to Triple(binding.cardShopping, binding.cardShopping.getChildAt(0) as ImageView, "#F1F5F9"),
            "Lainnya" to Triple(binding.cardMore, binding.cardMore.getChildAt(0) as ImageView, "#F1F5F9")
        )

        categoryViews.forEach { (catName, views) ->
            val card = views.first
            val icon = views.second
            
            if (catName == selectedCategory) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.voca_primary_blue))
                icon.imageTintList = ColorStateList.valueOf(Color.WHITE)
            } else {
                card.setCardBackgroundColor(Color.parseColor(views.third))
                icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#718096"))
            }
        }
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString()
        val title = binding.etTitle.text.toString().ifEmpty { selectedCategory }
        val type = if (binding.rbIncome.isChecked) "income" else "expense"
        
        if (amountStr.isNotEmpty()) {
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            
            // Format tanggal untuk SQLite (Lokal)
            val dateLocal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
            // Format tanggal untuk MySQL (Server YYYY-MM-DD)
            val dateServer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val userId = session.getUserId()

            // 1. Simpan ke Database Lokal (Agar langsung muncul di Home)
            db.addTransaction(userId, title, amount, type, selectedCategory, dateLocal)

            // 2. Simpan ke Server XAMPP
            val apiService = ApiService.getInstance()
            apiService.addTransaction(title, amount, type, selectedCategory, dateServer)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: retrofit2.Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@AddTransactionActivity, "Berhasil disimpan ke Server & Lokal", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            // Tetap finish karena sudah masuk lokal
                            Toast.makeText(this@AddTransactionActivity, "Tersimpan Lokal, Gagal Sinkron ke Server", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                        Toast.makeText(this@AddTransactionActivity, "Koneksi Gagal, Tersimpan di Lokal Saja", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
        } else {
            Toast.makeText(this, "Harap masukkan jumlah nominal", Toast.LENGTH_SHORT).show()
        }
    }
}