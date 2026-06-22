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
                    updatePasswordOnServer(email, newPass)
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

    private fun updatePasswordOnServer(email: String, newPass: String) {
        val apiService = com.example.voca.api.ApiService.create()
        apiService.updatePassword(email, newPass).enqueue(object : retrofit2.Callback<Map<String, Any>> {
            override fun onResponse(call: retrofit2.Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    // Update local DB too
                    db.updatePassword(email, newPass)
                    Toast.makeText(this@ChangePasswordActivity, "Kata sandi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.body()?.get("message")?.toString() ?: "Gagal memperbarui kata sandi di server"
                    Toast.makeText(this@ChangePasswordActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@ChangePasswordActivity, "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}