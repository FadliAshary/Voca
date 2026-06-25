package com.example.voca

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityAddGoalBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.text.DecimalFormat
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

        val currencyCode = session.getCurrency()
        binding.tvLabelTarget.text = "Harga Target ($currencyCode)"
        binding.tvLabelInitial.text = "Uang Terkumpul Awal ($currencyCode)"

        binding.toolbar.setNavigationOnClickListener { finish() }

        setupAmountFormatting(binding.etTargetAmount)
        setupAmountFormatting(binding.etInitialAmount)

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

    private fun setupAmountFormatting(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(".", "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = DecimalFormat("#,###").format(parsed).replace(",", ".")

                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)
                    } else {
                        current = ""
                    }

                    editText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun saveGoal() {
        val name = binding.etGoalName.text.toString()
        val targetStr = binding.etTargetAmount.text.toString().replace(".", "")
        val initialStr = binding.etInitialAmount.text.toString().replace(".", "")
        val deadline = binding.tvGoalDeadline.text.toString()
        val note = binding.etGoalNote.text.toString()

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
            
            val res = db.addGoal(userId, name, target, initial, deadline, note)
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