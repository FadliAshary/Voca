package com.example.voca

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityDetailTransactionBinding
import com.example.voca.model.Finance
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
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
    private var currentImagePath: String? = null
    private var calendar = Calendar.getInstance()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let { updateReceiptImage(it) }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                updateReceiptImage(bitmap)
            }
        }
    }

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

        binding.ivDetailReceipt.setOnClickListener {
            showImagePickDialog()
        }
        
        binding.layoutNoReceipt.setOnClickListener {
            showImagePickDialog()
        }
    }

    private fun showImagePickDialog() {
        val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Ubah Bukti Pembayaran")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    1 -> galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                }
            }
            .show()
    }

    private fun updateReceiptImage(bitmap: Bitmap) {
        currentImagePath = saveImageToInternalStorage(bitmap)
        binding.ivDetailReceipt.setImageBitmap(bitmap)
        binding.ivDetailReceipt.alpha = 1.0f
        binding.layoutNoReceipt.visibility = View.GONE
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
            val categories = arrayOf("Makanan", "Transport", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Pendidikan", "Rumah", "Hadiah", "Olahraga", "Travel", "Lainnya", "Gaji", "Bonus", "Investasi")
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
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
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
            binding.tvDetailType.setTextColor(android.graphics.Color.parseColor("#F56565"))
            binding.tvDetailType.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF5F5"))
            updateCategoryIcon(transactionCategory)
        }
    }

    private fun saveChanges() {
        val title = binding.etDetailMerchant.text.toString()
        val currencyCode = session.getCurrency()
        val amountStr = binding.etDetailAmount.text.toString()
            .replace(currencyCode, "")
            .replace("Rp", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        val amount = amountStr.toDoubleOrNull() ?: 0.0

        if (db.updateTransaction(transactionId, title, amount, transactionType, transactionCategory, transactionDate, currentImagePath) > 0) {
            Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTransactionDetails() {
        val t = db.getFinanceById(transactionId)
        if (t != null) {
            val title = t.title
            val amount = t.amount
            transactionCategory = t.category
            transactionDate = t.date
            transactionType = t.type
            currentImagePath = t.imagePath

            // Update calendar for DatePicker initial value
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
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
            if (!currentImagePath.isNullOrEmpty()) {
                val imgFile = File(currentImagePath!!)
                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    binding.ivDetailReceipt.setImageBitmap(myBitmap)
                    binding.ivDetailReceipt.alpha = 1.0f
                    binding.layoutNoReceipt.visibility = View.GONE
                }
            } else {
                binding.ivDetailReceipt.alpha = 0.3f
                binding.layoutNoReceipt.visibility = View.VISIBLE
            }

            // Set icon based on category
            updateCategoryIcon(transactionCategory)
        }
    }
}
