package com.example.voca

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.voca.databinding.ActivityLandingBinding
import com.example.voca.utils.SessionManager

class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Force light mode for this activity only
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        session = SessionManager(this)

        // Simulate splash loading
        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLoggedIn()) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 3000)
    }
}