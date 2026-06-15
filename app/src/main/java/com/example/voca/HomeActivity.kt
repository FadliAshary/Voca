package com.example.voca

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.voca.databinding.ActivityHomeBinding
import com.example.voca.utils.ReceiptScanner
import java.io.File
import java.io.FileOutputStream

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val scanner = ReceiptScanner()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

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
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        binding.fabAdd.setOnClickListener {
            showAddOptions()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_budget -> BudgetFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun showAddOptions() {
        val options = arrayOf("Input Manual", "Ambil Foto Struk", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Tambah Transaksi")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, AddTransactionActivity::class.java))
                    1 -> openCamera()
                    2 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun processReceipt(bitmap: Bitmap) {
        Toast.makeText(this, "Memproses struk...", Toast.LENGTH_SHORT).show()
        
        // Simpan gambar secara lokal agar bisa dikirim ke AddTransactionActivity
        val imagePath = saveImageToInternalStorage(bitmap)

        scanner.scanReceipt(bitmap,
            onSuccess = { fullText, amount ->
                val intent = Intent(this, AddTransactionActivity::class.java).apply {
                    if (amount != null) putExtra("EXTRA_AMOUNT", amount)
                    putExtra("EXTRA_OCR_TEXT", fullText)
                    putExtra("EXTRA_IMAGE_PATH", imagePath)
                }
                startActivity(intent)
            },
            onFailure = { e ->
                Toast.makeText(this, "Gagal mengenali struk: ${e.message}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AddTransactionActivity::class.java).apply {
                    putExtra("EXTRA_IMAGE_PATH", imagePath)
                }
                startActivity(intent)
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.liquid_enter, R.anim.liquid_exit)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}