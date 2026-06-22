package com.example.voca

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.voca.databinding.ActivityEditProfileBinding
import com.example.voca.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var session: SessionManager
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                // Take persistable permission to keep access after reboot
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedImageUri = it
                binding.ivEditProfile.setImageURI(it)
            } catch (e: Exception) {
                e.printStackTrace()
                // If it fails, just show it normally without persistable permission (might be from some apps)
                selectedImageUri = it
                binding.ivEditProfile.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setupUI()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        binding.btnNavChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.switchDarkMode.isChecked = session.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnChangePhoto.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }
    }

    private fun setupUI() {
        val currentName = session.getUserName()
        val currentStatus = session.getProfileStatus()
        val currentImage = session.getProfileImage()

        binding.etEditName.setText(currentName)
        binding.etEditStatus.setText(currentStatus)
        
        if (!currentImage.isNullOrEmpty()) {
            try {
                binding.ivEditProfile.setImageURI(Uri.parse(currentImage))
            } catch (e: Exception) {
                binding.ivEditProfile.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun saveProfile() {
        val newName = binding.etEditName.text.toString()
        val newStatus = binding.etEditStatus.text.toString()

        if (newName.isNotEmpty()) {
            session.setUserName(newName)
            session.setProfileStatus(newStatus)
            selectedImageUri?.let {
                session.setProfileImage(it.toString())
            }
            Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
        }
    }
}