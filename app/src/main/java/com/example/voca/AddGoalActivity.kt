package com.example.voca

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityAddGoalBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class AddGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddGoalBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnGoalDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.tvGoalDeadline.text = format.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSaveGoal.setOnClickListener {
            saveGoal()
        }
    }

    private fun saveGoal() {
        val name = binding.etGoalName.text.toString()
        val targetStr = binding.etTargetAmount.text.toString()
        val initialStr = binding.etInitialAmount.text.toString()
        val deadline = binding.tvGoalDeadline.text.toString()

        if (name.isNotEmpty() && targetStr.isNotEmpty()) {
            var target = targetStr.toDoubleOrNull() ?: 0.0
            var initial = initialStr.toDoubleOrNull() ?: 0.0
            val userId = session.getUserId()
            val currentCurrency = session.getCurrency()

            // Konversi ke IDR sebelum disimpan
            if (currentCurrency != "IDR") {
                target = CurrencyUtils.convertToIDR(target, currentCurrency)
                initial = CurrencyUtils.convertToIDR(initial, currentCurrency)
            }
            
            val res = db.addGoal(userId, name, target, initial, deadline)
            if (res > 0) {
                Toast.makeText(this, "Target Tabungan Berhasil Dibuat", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal Membuat Target", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Mohon lengkapi data", Toast.LENGTH_SHORT).show()
        }
    }
}