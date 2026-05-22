package com.example.voca

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.api.ApiService
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityRegisterBinding
import retrofit2.Callback
import retrofit2.Response

class RegisterActivityWithFallback : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            val name = binding.etRegName.text.toString()
            val email = binding.etRegEmail.text.toString()
            val pass = binding.etRegPassword.text.toString()
            val confirmPass = binding.etRegConfirmPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                if (pass == confirmPass) {
                    registerUserWithFallback(name, email, pass)
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

    /**
     * Register with fallback to local SQLite if server fails
     */
    private fun registerUserWithFallback(name: String, email: String, password: String) {
        val apiService = ApiService.create()

        // Try to register via API first
        apiService.register(name, email, password)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: retrofit2.Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    android.util.Log.d("RegisterFallback", "Server Response: ${response.code()}")

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body["success"] == true) {
                            // Success from server
                            Toast.makeText(
                                this@RegisterActivityWithFallback,
                                "Registrasi Berhasil (Tersimpan di Server)",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            // Server error response
                            val message = body?.get("message")?.toString() ?: "Registrasi Gagal"
                            Toast.makeText(this@RegisterActivityWithFallback, message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Server error - fallback to SQLite
                        fallbackToLocalDatabase(name, email, password)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                    // Connection failed - fallback to SQLite
                    android.util.Log.e("RegisterFallback", "Connection Error: ${t.message}", t)
                    fallbackToLocalDatabase(name, email, password)
                }
            })
    }

    /**
     * Fallback: Save to local SQLite database
     */
    private fun fallbackToLocalDatabase(name: String, email: String, password: String) {
        val res = db.addUser(name, email, password)
        if (res > 0) {
            Toast.makeText(
                this,
                "⚠️ Server tidak accessible\nData disimpan di perangkat lokal",
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            Toast.makeText(this, "❌ Registrasi Gagal", Toast.LENGTH_SHORT).show()
        }
    }
}

