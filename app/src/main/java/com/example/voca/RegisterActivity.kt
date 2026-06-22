package com.example.voca

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.voca.api.ApiService
import com.example.voca.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force light mode for registration
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etRegName.text.toString()
            val email = binding.etRegEmail.text.toString()
            val pass = binding.etRegPassword.text.toString()
            val confirmPass = binding.etRegConfirmPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                if (!email.endsWith("@gmail.com")) {
                    Toast.makeText(this, "Email harus menggunakan @gmail.com", Toast.LENGTH_SHORT).show()
                } else if (pass == confirmPass) {
                    registerUser(name, email, pass)
                } else {
                    Toast.makeText(this, "Kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        // PERBAIKAN: Menggunakan getInstance() sesuai arsitektur ApiService terbaru
        val apiService = ApiService.getInstance()

        // Show loading toast
        Toast.makeText(this, "Sedang mengirim data ke server...", Toast.LENGTH_SHORT).show()

        apiService.register(name, email, password)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>, // Disederhanakan menggunakan import retrofit2.Call
                    response: Response<Map<String, Any>>
                ) {
                    Log.d("RegisterActivity", "Response: ${response.code()} - ${response.body()}")

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body["success"] == true) {
                            // Cadangkan ke DB lokal untuk fitur Ganti Kata Sandi
                            val dbHelper = com.example.voca.database.DatabaseHelper(this@RegisterActivity)
                            dbHelper.saveOrUpdateUser(name, email, password)

                            Toast.makeText(this@RegisterActivity, "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            val message = body?.get("message")?.toString() ?: "Registrasi Gagal"
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    val errorMsg = t.message ?: "Unknown error"
                    Log.e("RegisterActivity", "Failure: $errorMsg", t)
                    Toast.makeText(this@RegisterActivity, "Connection Failed:\n$errorMsg", Toast.LENGTH_LONG).show()
                }
            })
    }
}