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
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
import java.text.NumberFormat
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

    private var currentAmountText = ""

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

        // Make status bar light
        window.statusBarColor = ContextCompat.getColor(this, R.color.voca_light_gray)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        db = DatabaseHelper(this)
        session = com.example.voca.utils.SessionManager(this)

        setupToolbar()
        setupCategorySelection()
        setupDatePicker()
        setupAccountPicker()
        setupUploadReceipt()
        setupAmountFormatting()
        
        updateDateDisplay()
        handleIntentExtras()

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupAmountFormatting() {
        binding.etAmount.addTextChangedListener { s ->
            if (s.toString() != currentAmountText) {
                binding.etAmount.removeTextChangedListener(null) // Not working like this in Kotlin extension
            }
        }
        
        // Manual implementation for better control
        binding.etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.toString() != currentAmountText) {
                    binding.etAmount.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(".", "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatted = NumberFormat.getInstance(Locale("in", "ID")).format(parsed)

                        currentAmountText = formatted
                        binding.etAmount.setText(formatted)
                        binding.etAmount.setSelection(formatted.length)
                    } else {
                        currentAmountText = ""
                        binding.etAmount.setText("")
                    }

                    binding.etAmount.addTextChangedListener(this)
                }
            }
        })
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
        // Expense Categories
        binding.catFood.setOnClickListener { selectCategory("Makanan") }
        binding.catTransport.setOnClickListener { selectCategory("Transport") }
        binding.catShopping.setOnClickListener { selectCategory("Belanja") }
        binding.catBill.setOnClickListener { selectCategory("Tagihan") }
        binding.catEntertainment.setOnClickListener { selectCategory("Hiburan") }
        binding.catHealth.setOnClickListener { selectCategory("Kesehatan") }
        binding.catEducation.setOnClickListener { selectCategory("Pendidikan") }
        binding.catHome.setOnClickListener { selectCategory("Rumah") }
        binding.catGift.setOnClickListener { selectCategory("Hadiah") }
        binding.catSport.setOnClickListener { selectCategory("Olahraga") }
        binding.catTravel.setOnClickListener { selectCategory("Travel") }
        binding.catMore.setOnClickListener { selectCategory("Lainnya") }

        // Income Categories
        binding.catSalary.setOnClickListener { selectCategory("Gaji") }
        binding.catBonus.setOnClickListener { selectCategory("Bonus") }
        binding.catInvestment.setOnClickListener { selectCategory("Investasi") }
        binding.catMoreIncome.setOnClickListener { selectCategory("Lainnya") }

        // Type Toggle
        binding.rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbIncome) {
                binding.gridExpenseCategories.visibility = View.GONE
                binding.gridIncomeCategories.visibility = View.VISIBLE
                selectCategory("Gaji")
            } else {
                binding.gridExpenseCategories.visibility = View.VISIBLE
                binding.gridIncomeCategories.visibility = View.GONE
                selectCategory("Makanan")
            }
        }
        
        // Initial selection
        selectCategory("Makanan")
    }

    private fun selectCategory(name: String) {
        selectedCategory = name
        
        // Handle "Lainnya" based on current type
        val isIncome = binding.rbIncome.isChecked
        val cardMoreToUse = if (isIncome) binding.cardMoreIncome else binding.cardMore
        
        // Reset ALL cards first
        val allCards = listOf(
            binding.cardFood, binding.cardTransport, binding.cardShopping, binding.cardBill,
            binding.cardEntertainment, binding.cardHealth, binding.cardEducation, binding.cardHome,
            binding.cardGift, binding.cardSport, binding.cardTravel, binding.cardMore,
            binding.cardSalary, binding.cardBonus, binding.cardInvestment, binding.cardMoreIncome
        )

        allCards.forEach { card ->
            val icon = card.getChildAt(0) as ImageView
            card.setCardBackgroundColor(Color.parseColor("#F1F5F9"))
            icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#718096"))
        }

        // Highlight selected
        val selectedCard = when(name) {
            "Makanan" -> binding.cardFood
            "Transport" -> binding.cardTransport
            "Belanja" -> binding.cardShopping
            "Tagihan" -> binding.cardBill
            "Hiburan" -> binding.cardEntertainment
            "Kesehatan" -> binding.cardHealth
            "Pendidikan" -> binding.cardEducation
            "Rumah" -> binding.cardHome
            "Hadiah" -> binding.cardGift
            "Olahraga" -> binding.cardSport
            "Travel" -> binding.cardTravel
            "Gaji" -> binding.cardSalary
            "Bonus" -> binding.cardBonus
            "Investasi" -> binding.cardInvestment
            "Lainnya" -> cardMoreToUse
            else -> binding.cardMore
        }

        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.voca_primary_blue))
        (selectedCard.getChildAt(0) as ImageView).imageTintList = ColorStateList.valueOf(Color.WHITE)
    }

    private fun saveTransaction() {
        val amountStr = binding.etAmount.text.toString().replace(".", "")
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

            // 1. Simpan ke Database Lokal (Status Sync Awal = 0)
            val localId = db.addTransaction(userId, title, amount, type, selectedCategory, dateLocal, currentImagePath, isSynced = 0)

            // 2. Simpan ke Server XAMPP
            val apiService = ApiService.getInstance()
            apiService.addTransaction(userId, title, amount, type, selectedCategory, dateServer)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: retrofit2.Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        if (response.isSuccessful) {
                            // Update status sync di lokal jika berhasil
                            val responseBody = response.body()
                            val remoteId = (responseBody?.get("id") as? Double)?.toInt() ?: 0
                            db.markTransactionSynced(localId.toInt(), remoteId)
                            
                            Toast.makeText(this@AddTransactionActivity, "Berhasil disinkronkan ke Server", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AddTransactionActivity, "Tersimpan Lokal, Akan disinkronkan nanti", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                        Toast.makeText(this@AddTransactionActivity, "Offline: Tersimpan di Lokal", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
        } else {
            Toast.makeText(this, "Harap masukkan jumlah nominal", Toast.LENGTH_SHORT).show()
        }
    }
}