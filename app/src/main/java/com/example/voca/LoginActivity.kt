package com.example.voca

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.api.ApiService
import com.example.voca.databinding.ActivityLoginBinding
import com.example.voca.utils.SessionManager
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force light mode for login
        delegate.localNightMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val pass = binding.etLoginPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginUser(email, pass)
            } else {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val text = "Belum punya akun? Daftar Sekarang"
        val spannable = android.text.SpannableString(text)
        val blueColor = android.graphics.Color.parseColor("#003285")
        val start = text.indexOf("Daftar Sekarang")
        if (start != -1) {
            spannable.setSpan(
                android.text.style.ForegroundColorSpan(blueColor),
                start,
                start + "Daftar Sekarang".length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start,
                start + "Daftar Sekarang".length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.tvRegisterLink.text = spannable
    }

    private fun loginUser(email: String, password: String) {
        val apiService = ApiService.create()
        apiService.login(email, password)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: retrofit2.Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body["success"] == true) {
                            session.setLogin(true)
                            
                            // Ambil ID dari server dan simpan ke session
                            val userIdStr = body["id"]?.toString() ?: "-1"
                            val userId = userIdStr.toDoubleOrNull()?.toInt() ?: -1
                            session.setUserId(userId)

                            session.setUserName(body["name"]?.toString() ?: email)
                            session.setUserEmail(email)

                            // Save user to local DB for Change Password feature
                            val db = com.example.voca.database.DatabaseHelper(this@LoginActivity)
                            db.saveOrUpdateUser(body["name"]?.toString() ?: "User", email, password)

                            Toast.makeText(this@LoginActivity, "Login Berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finishAffinity()
                        } else {
                            val message = body?.get("message")?.toString() ?: "Login Gagal"
                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Gagal", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}