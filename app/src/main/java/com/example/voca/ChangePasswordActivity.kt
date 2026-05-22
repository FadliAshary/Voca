package com.example.voca

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityChangePasswordBinding
import com.example.voca.utils.SessionManager

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSaveNewPassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPass = binding.etOldPassword.text.toString()
        val newPass = binding.etNewPassword.text.toString()
        val confirmPass = binding.etConfirmNewPassword.text.toString()
        val email = session.getUserEmail() ?: ""

        if (oldPass.isNotEmpty() && newPass.isNotEmpty() && confirmPass.isNotEmpty()) {
            if (newPass == confirmPass) {
                // First check if old password is correct
                if (db.checkUser(email, oldPass)) {
                    val res = db.updatePassword(email, newPass)
                    if (res > 0) {
                        Toast.makeText(this, "Kata sandi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal memperbarui kata sandi", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Kata sandi lama salah", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Kata sandi baru tidak cocok", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Mohon isi semua field", Toast.LENGTH_SHORT).show()
        }
    }
}