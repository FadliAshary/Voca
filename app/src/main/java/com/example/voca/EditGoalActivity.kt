package com.example.voca

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.ActivityEditGoalBinding
import java.text.NumberFormat
import java.util.*

class EditGoalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGoalBinding
    private lateinit var db: DatabaseHelper
    private var goalId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGoalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        goalId = intent.getIntExtra("GOAL_ID", -1)

        if (goalId != -1) {
            loadGoalData()
        } else {
            finish()
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnDeleteGoal.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnSaveGoalUpdate.setOnClickListener {
            saveUpdate()
        }
    }

    private fun loadGoalData() {
        val goals = db.getAllGoals()
        val goal = goals.find { (it["id"] as Int) == goalId }
        
        if (goal != null) {
            val name = goal["name"] as String
            val target = goal["target_amount"] as Double
            val current = goal["current_amount"] as Double

            binding.tvEditGoalNameDisplay.text = name
            binding.etUpdateCurrentAmount.setText(current.toString())
            
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvGoalProgressSummary.text = "Target: ${formatter.format(target).replace("Rp", "Rp ")}"
        }
    }

    private fun saveUpdate() {
        val amountStr = binding.etUpdateCurrentAmount.text.toString()
        if (amountStr.isNotEmpty()) {
            val newAmount = amountStr.toDoubleOrNull() ?: 0.0
            if (db.updateGoalAmount(goalId, newAmount) > 0) {
                Toast.makeText(this, "Progress tabungan diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
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