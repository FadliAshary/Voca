package com.example.voca

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityEditGoalBinding
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class EditGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGoalBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: com.example.voca.utils.SessionManager
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
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvCurrentAmountDisplay.text = formatter.format(originalAmount).replace("Rp", "Rp ")
            binding.tvNewTotalPreview.text = formatter.format(originalAmount).replace("Rp", "Rp ")
            binding.tvGoalProgressSummary.text = "Target: ${formatter.format(targetAmount).replace("Rp", "Rp ")}"
            
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
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = DecimalFormat("#,###").format(parsed).replace(",", ".")
                        
                        current = formatted
                        binding.etUpdateCurrentAmount.setText(formatted)
                        binding.etUpdateCurrentAmount.setSelection(formatted.length)
                        
                        // Update preview
                        val newTotal = originalAmount + parsed
                        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                        binding.tvNewTotalPreview.text = formatter.format(newTotal).replace("Rp", "Rp ")
                    } else {
                        current = ""
                        binding.tvNewTotalPreview.text = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                            .format(originalAmount).replace("Rp", "Rp ")
                    }

                    binding.etUpdateCurrentAmount.addTextChangedListener(this)
                }
            }
        })
    }

    private fun saveUpdate() {
        val addAmountStr = binding.etUpdateCurrentAmount.text.toString().replace(".", "")
        val addAmount = if (addAmountStr.isNotEmpty()) addAmountStr.toDouble() else 0.0
        val newTotal = originalAmount + addAmount

        if (db.updateGoalAmount(goalId, newTotal) > 0) {
            Toast.makeText(this, "Progress tabungan diperbarui (+${NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(addAmount).replace("Rp", "Rp ")})", Toast.LENGTH_SHORT).show()
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