package com.example.voca

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.voca.api.ApiService
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityAddTransactionBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.ReceiptScanner
import com.example.voca.utils.SessionManager
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var selectedCategory = "Makanan"
    private var calendar = Calendar.getInstance()
    private var selectedAccount = "Tabungan Utama"
    private var currentImagePath: String? = null
    private val scanner = ReceiptScanner()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let { processReceipt(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                processReceipt(bitmap)
            }
        }
    }

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
        handleIntentExtras()

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun handleIntentExtras() {
        val amount = intent.getDoubleExtra("EXTRA_AMOUNT", -1.0)
        if (amount != -1.0) {
            // Hilangkan .0 jika itu bilangan bulat agar lebih enak dilihat
            val displayAmount = if (amount % 1 == 0.0) amount.toLong().toString() else amount.toString()
            binding.etAmount.setText(displayAmount)
        }
        
        val ocrText = intent.getStringExtra("EXTRA_OCR_TEXT")
        if (!ocrText.isNullOrEmpty()) {
            // Coba tebak judul dari baris pertama teks OCR yang bukan angka
            val firstLine = ocrText.split("\n").firstOrNull { it.length > 3 && !it.any { c -> c.isDigit() } }
            if (firstLine != null) {
                binding.etTitle.setText(firstLine.trim())
            }
        }

        val imagePath = intent.getStringExtra("EXTRA_IMAGE_PATH")
        if (!imagePath.isNullOrEmpty()) {
            currentImagePath = imagePath
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                binding.ivReceiptPreview.setImageBitmap(myBitmap)
                binding.ivReceiptPreview.visibility = View.VISIBLE
                binding.layoutUploadPlaceholder.visibility = View.GONE
            }
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
            val options = arrayOf("Ambil Foto Struk", "Pilih dari Galeri")
            AlertDialog.Builder(this)
                .setTitle("Scan Struk Otomatis")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                        1 -> galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                    }
                }
                .show()
        }
    }

    private fun processReceipt(bitmap: Bitmap) {
        Toast.makeText(this, "Menganalisis struk...", Toast.LENGTH_SHORT).show()

        // Simpan gambar secara lokal
        currentImagePath = saveImageToInternalStorage(bitmap)
        binding.ivReceiptPreview.setImageBitmap(bitmap)
        binding.ivReceiptPreview.visibility = View.VISIBLE
        binding.layoutUploadPlaceholder.visibility = View.GONE

        scanner.scanReceipt(bitmap,
            onSuccess = { _, amount ->
                if (amount != null) {
                    val displayAmount = if (amount % 1 == 0.0) amount.toLong().toString() else amount.toString()
                    val currentInput = binding.etAmount.text.toString()

                    // Jika input masih kosong atau 0, langsung isi
                    if (currentInput.isEmpty() || currentInput == "0") {
                        binding.etAmount.setText(displayAmount)
                        Toast.makeText(this, "Total ditemukan: Rp $displayAmount", Toast.LENGTH_SHORT).show()
                    } else {
                        // Jika sudah ada isinya, tanya user dulu
                        AlertDialog.Builder(this)
                            .setTitle("Struk Terdeteksi")
                            .setMessage("Kami menemukan nominal Rp $displayAmount pada struk. Apakah Anda ingin mengganti nominal yang sudah Anda ketik?")
                            .setPositiveButton("Ganti") { _, _ ->
                                binding.etAmount.setText(displayAmount)
                            }
                            .setNegativeButton("Tetap Gunakan Input Saya", null)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "Gagal menemukan total harga, silakan isi manual", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val fileName = "receipt_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(null), fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
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
            var amount = amountStr.toDoubleOrNull() ?: 0.0
            
            // Konversi input ke IDR (base) sebelum disimpan ke database
            val currentCurrency = session.getCurrency()
            if (currentCurrency != "IDR") {
                amount = CurrencyUtils.convertToIDR(amount, currentCurrency)
            }
            
            // Format tanggal untuk SQLite (Lokal)
            val dateLocal = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
            // Format tanggal untuk MySQL (Server YYYY-MM-DD)
            val dateServer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            
            val userId = session.getUserId()

            // 1. Simpan ke Database Lokal (Agar langsung muncul di Home)
            db.addTransaction(userId, title, amount, type, selectedCategory, dateLocal, currentImagePath)

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