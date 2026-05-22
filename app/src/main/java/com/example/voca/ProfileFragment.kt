package com.example.voca

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.FragmentProfileBinding
import com.example.voca.utils.SessionManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager
    private lateinit var db: DatabaseHelper
    private var savingsTarget = 75.0 
    private var currentFilter = "Bulanan"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())
        db = DatabaseHelper(requireContext())

        updateProfileUI()
        
        binding.tvTargetLabel.setOnClickListener {
            showSetTargetDialog()
        }

        binding.btnAccountSettings.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        
        binding.ivProfileLarge.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.btnFilterRatio.setOnClickListener {
            showFilterDialog()
        }

        updateStats()

        binding.btnLogout.setOnClickListener {
            session.logout()
            startActivity(Intent(requireContext(), LandingActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("Mingguan", "Bulanan", "Tahunan")
        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Periode")
            .setItems(options) { _, which ->
                currentFilter = options[which]
                binding.btnFilterRatio.text = currentFilter
                updateStats()
            }
            .show()
    }

    private fun updateProfileUI() {
        val fullName = session.getUserName() ?: "User"
        val displayName = if (fullName.contains("@")) fullName.split("@")[0] else fullName
        binding.tvProfileName.text = displayName
        binding.tvProfileStatus.text = session.getProfileStatus()
        
        val imageUriStr = session.getProfileImage()
        if (!imageUriStr.isNullOrEmpty()) {
            try {
                binding.ivProfileLarge.setImageURI(Uri.parse(imageUriStr))
            } catch (e: Exception) {
                binding.ivProfileLarge.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun showSetTargetDialog() {
        val input = EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Contoh: 75"

        AlertDialog.Builder(requireContext())
            .setTitle("Atur Target Tabungan (%)")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val newTarget = input.text.toString().toDoubleOrNull()
                if (newTarget != null && newTarget in 0.0..100.0) {
                    savingsTarget = newTarget
                    updateStats()
                } else {
                    Toast.makeText(requireContext(), "Target tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateProfileUI()
        updateStats()
    }

    private fun getCurrencyFormatter(): NumberFormat {
        val currencyCode = session.getCurrency()
        return when (currencyCode) {
            "USD" -> NumberFormat.getCurrencyInstance(Locale.US)
            "EUR" -> NumberFormat.getCurrencyInstance(Locale.GERMANY)
            "JPY" -> NumberFormat.getCurrencyInstance(Locale.JAPAN)
            else -> NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        }
    }

    private fun updateStats() {
        val allTransactions = db.getAllTransactions()
        val goals = db.getAllGoals()
        
        val filteredTransactions = filterTransactionsByPeriod(allTransactions)
        
        var periodIncome = 0.0
        var periodExpense = 0.0
        for (t in filteredTransactions) {
            val amount = t["amount"] as Double
            if (t["type"] == "income") periodIncome += amount else periodExpense += amount
        }

        var totalIncome = 0.0
        var totalExpense = 0.0
        for (t in allTransactions) {
            val amount = t["amount"] as Double
            if (t["type"] == "income") totalIncome += amount else totalExpense += amount
        }

        val netWorth = totalIncome - totalExpense
        val totalSavings = goals.sumOf { it["current_amount"] as Double }

        val formatter = getCurrencyFormatter()
        binding.tvNetWorth.text = formatter.format(netWorth).replace("Rp", "Rp ")
        
        if (netWorth >= 0) {
            binding.tvNetWorth.setTextColor(android.graphics.Color.parseColor("#003285"))
            val trend = if (totalExpense > 0) (netWorth/totalExpense)*100 else 0.0
            binding.tvNetWorthTrend.text = "↗ +${String.format(Locale.getDefault(), "%.1f%%", trend)} Global"
            binding.tvNetWorthTrend.setTextColor(android.graphics.Color.parseColor("#10B981"))
        } else {
            binding.tvNetWorth.setTextColor(android.graphics.Color.parseColor("#EF4444"))
            val trend = if (totalIncome > 0) (netWorth/totalIncome)*100 else 0.0
            binding.tvNetWorthTrend.text = "↘ ${String.format(Locale.getDefault(), "%.1f%%", trend)} Global"
            binding.tvNetWorthTrend.setTextColor(android.graphics.Color.parseColor("#EF4444"))
        }

        val savingsRatio = if (netWorth > 0) {
            (totalSavings / netWorth * 100).coerceAtMost(100.0)
        } else {
            0.0
        }
        
        binding.tvSavingsRatio.text = String.format(Locale.getDefault(), "%.1f%%", savingsRatio)
        binding.pbSavingsRatio.progress = savingsRatio.toInt().coerceIn(0, 100)
        binding.tvTargetLabel.text = "Target: ${savingsTarget.toInt()}%"
    }

    private fun filterTransactionsByPeriod(transactions: List<Map<String, Any>>): List<Map<String, Any>> {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val now = Calendar.getInstance()
        
        return transactions.filter { t ->
            val dateStr = t["date"] as String
            try {
                val date = sdf.parse(dateStr) ?: return@filter false
                val cal = Calendar.getInstance().apply { time = date }
                
                when (currentFilter) {
                    "Mingguan" -> {
                        now.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR) &&
                        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    }
                    "Bulanan" -> {
                        now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    }
                    "Tahunan" -> {
                        now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    }
                    else -> true
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}