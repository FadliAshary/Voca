package com.example.voca

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityEditGoalBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.text.DecimalFormat
import java.util.*

class EditGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGoalBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var goalId: Int = -1
    private var originalAmount: Double = 0.0
    private var targetAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = com.example.voca.utils.SessionManager(this)
        goalId = intent.getIntExtra("GOAL_ID", -1)

        val currencyCode = session.getCurrency()
        binding.tvLabelAddAmount.text = "Tambah Nominal ($currencyCode)"

        if (goalId != -1) {
            loadGoalData()
        } else {
            finish()
        }

        setupTextWatcher()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnDeleteGoal.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnSaveGoalUpdate.setOnClickListener {
            saveUpdate()
        }
    }

    private fun loadGoalData() {
        val userId = session.getUserId()
        val goals = db.getAllGoals(userId)
        val goal = goals.find { (it["id"] as Int) == goalId }
        
        if (goal != null) {
            val name = goal["name"] as String
            targetAmount = goal["target_amount"] as Double
            originalAmount = goal["current_amount"] as Double

            binding.tvEditGoalNameDisplay.text = name
            
            val currencyCode = session.getCurrency()
            binding.tvCurrentAmountDisplay.text = CurrencyUtils.formatCurrency(originalAmount, currencyCode)
            binding.tvNewTotalPreview.text = CurrencyUtils.formatCurrency(originalAmount, currencyCode)
            binding.tvGoalProgressSummary.text = "Target: ${CurrencyUtils.formatCurrency(targetAmount, currencyCode)}"
            
            binding.etUpdateCurrentAmount.setText("")
        }
    }

    private fun setupTextWatcher() {
        binding.etUpdateCurrentAmount.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.etUpdateCurrentAmount.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(".", "")
                    val currencyCode = session.getCurrency()
                    if (cleanString.isNotEmpty()) {
                        val parsedInput = cleanString.toDouble()
                        val formatted = DecimalFormat("#,###").format(parsedInput).replace(",", ".")
                        
                        current = formatted
                        binding.etUpdateCurrentAmount.setText(formatted)
                        binding.etUpdateCurrentAmount.setSelection(formatted.length)
                        
                        // Konversi input ke IDR untuk perhitungan preview jika bukan IDR
                        val parsedIDR = CurrencyUtils.convertToIDR(parsedInput, currencyCode)
                        
                        // Update preview
                        val newTotal = originalAmount + parsedIDR
                        binding.tvNewTotalPreview.text = CurrencyUtils.formatCurrency(newTotal, currencyCode)
                    } else {
                        current = ""
                        binding.tvNewTotalPreview.text = CurrencyUtils.formatCurrency(originalAmount, currencyCode)
                    }

                    binding.etUpdateCurrentAmount.addTextChangedListener(this)
                }
            }
        })
    }

    private fun saveUpdate() {
        val addAmountStr = binding.etUpdateCurrentAmount.text.toString().replace(".", "")
        var addAmount = if (addAmountStr.isNotEmpty()) addAmountStr.toDouble() else 0.0
        
        val currencyCode = session.getCurrency()
        val addAmountFormatted = CurrencyUtils.formatCurrency(CurrencyUtils.convertToIDR(addAmount, currencyCode), currencyCode)
        
        // Konversi ke IDR sebelum ditambahkan ke total database
        if (currencyCode != "IDR") {
            addAmount = CurrencyUtils.convertToIDR(addAmount, currencyCode)
        }
        
        val newTotal = originalAmount + addAmount

        if (db.updateGoalAmount(goalId, newTotal) > 0) {
            Toast.makeText(this, "Progress tabungan diperbarui (+$addAmountFormatted)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Target")
            .setMessage("Apakah Anda yakin ingin menghapus target tabungan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                if (db.deleteGoal(goalId) > 0) {
                    Toast.makeText(this, "Target dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}