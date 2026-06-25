package com.example.voca

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.FragmentBudgetBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {
    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        session = SessionManager(requireContext())
        
        binding.btnAddGoal.setOnClickListener {
            startActivity(Intent(requireContext(), AddGoalActivity::class.java))
        }

        binding.cardBudgetBalance.setOnClickListener {
            showSetBudgetTargetDialog()
        }

        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun showSetBudgetTargetDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_set_budget, null)
        val etAmount = view.findViewById<EditText>(R.id.etDialogBudgetAmount)
        val btnWeekly = view.findViewById<TextView>(R.id.tvDialogWeekly)
        val btnMonthly = view.findViewById<TextView>(R.id.tvDialogMonthly)
        val btnRefresh = view.findViewById<View>(R.id.btnDialogRefresh)
        
        var selectedType = session.getBudgetType()
        val currencyCode = session.getCurrency()
        val currentTargetRaw = session.getBudgetTarget().toDouble()
        val currentTarget = CurrencyUtils.convertFromIDR(currentTargetRaw, currencyCode)

        val tvNominalLabel = view.findViewById<TextView>(R.id.tvDialogNominalLabel)
        tvNominalLabel.text = "Nominal Target ($currencyCode)"
        
        // Cek apakah budget sudah tercapai untuk menampilkan tombol refresh
        val userId = session.getUserId()
        val transactions = db.getAllTransactions(userId)
        val budgetOffset = session.getBudgetOffset().toDouble()
        val rawExpense = calculateCurrentPeriodExpense(transactions, selectedType)
        val currentDisplayExpense = (rawExpense - budgetOffset).coerceAtLeast(0.0)
        
        if (currentTarget > 0 && currentDisplayExpense >= currentTarget) {
            btnRefresh.visibility = View.VISIBLE
        } else {
            btnRefresh.visibility = View.GONE
        }

        // Format nominal awal dengan titik
        if (currentTarget > 0) {
            etAmount.setText(DecimalFormat("#,###").format(currentTarget).replace(",", "."))
        }

        // Add TextWatcher for dot formatting
        etAmount.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etAmount.removeTextChangedListener(this)
                    val cleanString = s.toString().replace(".", "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = DecimalFormat("#,###").format(parsed).replace(",", ".")
                        current = formatted
                        etAmount.setText(formatted)
                        etAmount.setSelection(formatted.length)
                    } else {
                        current = ""
                    }
                    etAmount.addTextChangedListener(this)
                }
            }
        })

        val updateSelection = {
            if (selectedType == "Mingguan") {
                btnWeekly.setBackgroundResource(R.drawable.logo_rounded_bg)
                btnWeekly.setTextColor(android.graphics.Color.WHITE)
                btnMonthly.setBackgroundResource(0)
                btnMonthly.setTextColor(android.graphics.Color.GRAY)
            } else {
                btnMonthly.setBackgroundResource(R.drawable.logo_rounded_bg)
                btnMonthly.setTextColor(android.graphics.Color.WHITE)
                btnWeekly.setBackgroundResource(0)
                btnWeekly.setTextColor(android.graphics.Color.GRAY)
            }
        }
        updateSelection()

        btnWeekly.setOnClickListener { selectedType = "Mingguan"; updateSelection() }
        btnMonthly.setOnClickListener { selectedType = "Bulanan"; updateSelection() }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(view)
            .create()

        view.findViewById<View>(R.id.btnDialogCancel).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btnDialogSave).setOnClickListener {
            val amountStr = etAmount.text.toString().replace(".", "")
            var amount = amountStr.toFloatOrNull() ?: 0f
            
            // Konversi kembali ke IDR sebelum disimpan
            if (currencyCode != "IDR") {
                amount = CurrencyUtils.convertToIDR(amount.toDouble(), currencyCode).toFloat()
            }

            session.setBudgetTarget(amount)
            session.setBudgetType(selectedType)
            // Hapus reset date dan offset jika target diubah/diset baru
            session.setBudgetResetDate("") 
            session.setBudgetOffset(0f)
            updateUI()
            dialog.dismiss()
        }

        btnRefresh.setOnClickListener {
            val userId = session.getUserId()
            val transactions = db.getAllTransactions(userId)
            val currentTotal = calculateCurrentPeriodExpense(transactions, selectedType)
            session.setBudgetOffset(currentTotal.toFloat())
            updateUI()
            dialog.dismiss()
            android.widget.Toast.makeText(requireContext(), "Anggaran berhasil di-refresh (dimulai dari 0)", android.widget.Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun calculateCurrentPeriodExpense(transactions: List<Map<String, Any>>, budgetType: String): Double {
        val now = Calendar.getInstance()

        return transactions.filter { t ->
            val dateStr = t["date"] as String
            val type = t["type"] as String
            if (type != "expense") return@filter false
            
            try {
                val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dateStr) ?: return@filter false
                val cal = Calendar.getInstance().apply { time = date }
                if (budgetType == "Mingguan") {
                    now.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR) &&
                    now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                } else {
                    now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }
            } catch (e: Exception) { false }
        }.sumOf { it["amount"] as Double }
    }

    private fun getCurrencyFormatter(): NumberFormat {
        return CurrencyUtils.getFormatter(session.getCurrency())
    }

    private fun updateUI() {
        val userId = session.getUserId()
        val transactions = db.getAllTransactions(userId)
        
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        val now = Calendar.getInstance()
        
        val budgetType = session.getBudgetType()
        val budgetOffset = session.getBudgetOffset().toDouble()

        val filteredTransactions = transactions.filter { t ->
            val dateStr = t["date"] as String
            try {
                val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dateStr) ?: return@filter false
                val cal = Calendar.getInstance().apply { time = date }
                if (budgetType == "Mingguan") {
                    now.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR) &&
                    now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                } else {
                    now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }
            } catch (e: Exception) { false }
        }

        val categoryExpenses = mutableMapOf<String, Double>()
        for (t in filteredTransactions) {
            if (t["type"] == "expense") {
                val category = t["category"] as? String ?: "Lainnya"
                categoryExpenses[category] = (categoryExpenses[category] ?: 0.0) + (t["amount"] as Double)
            }
        }

        val budgetTarget = session.getBudgetTarget().toDouble()
        val totalExpense = filteredTransactions.filter { it["type"] == "expense" }.sumOf { it["amount"] as Double }
        
        // Terapkan offset refresh
        val currentPeriodExpense = (totalExpense - budgetOffset).coerceAtLeast(0.0)
        val remainingBudget = (budgetTarget - currentPeriodExpense).coerceAtLeast(0.0)

        val currencyCode = session.getCurrency()
        binding.tvMonth.text = "${monthFormat.format(Date())} ($budgetType)"
        binding.tvRemainingBudget.text = CurrencyUtils.formatCurrency(remainingBudget, currencyCode)
        
        val percentUsed = if (budgetTarget > 0) ((currentPeriodExpense / budgetTarget) * 100).toInt().coerceIn(0, 100) else 0
        binding.pbBudget.progress = percentUsed
        
        val expenseStr = CurrencyUtils.formatCurrency(currentPeriodExpense, currencyCode)
        val targetStr = CurrencyUtils.formatCurrency(budgetTarget, currencyCode)
        binding.tvBudgetStatus.text = "$percentUsed% terpakai ($expenseStr dari target $targetStr)"

        updateCategoryList(categoryExpenses)
        updateGoalsList()
    }

    private fun updateCategoryList(categoryExpenses: Map<String, Double>) {
        binding.categoryContainer.removeAllViews()
        val categoryTargets = mapOf(
            "Makanan" to 3000000.0,
            "Transport" to 1500000.0,
            "Belanja" to 4000000.0,
            "Tagihan" to 2000000.0,
            "Hiburan" to 1000000.0,
            "Kesehatan" to 1500000.0,
            "Pendidikan" to 2500000.0,
            "Rumah" to 3000000.0,
            "Hadiah" to 500000.0,
            "Olahraga" to 500000.0,
            "Travel" to 2000000.0,
            "Lainnya" to 1000000.0
        )
        
        // Include ALL categories that have expenses, plus core defaults
        val allCategoriesWithExpenses = categoryExpenses.keys
        val defaultCategories = categoryTargets.keys
        val combinedCategories = (allCategoriesWithExpenses + defaultCategories).distinct()
        
        val sortedCategories = combinedCategories.sortedByDescending { categoryExpenses[it] ?: 0.0 }

        sortedCategories.forEach { name ->
            val expense = categoryExpenses[name] ?: 0.0
            val target = categoryTargets[name] ?: 1000000.0 // Default target for unlisted
            
            // Only show category if there is expense OR it's one of the top 4 default categories
            if (expense > 0 || name in listOf("Makanan", "Transport", "Belanja", "Lainnya")) {
                val itemView = layoutInflater.inflate(R.layout.item_budget_category, binding.categoryContainer, false)
                val tvName = itemView.findViewById<TextView>(R.id.tvCatName)
                val pbCat = itemView.findViewById<ProgressBar>(R.id.pbCatBudget)
                val tvPercent = itemView.findViewById<TextView>(R.id.tvCatPercent)
                val ivIcon = itemView.findViewById<ImageView>(R.id.ivCatIcon)
                val cardIcon = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCatIcon)

                tvName.text = name
                val percent = ((expense / target) * 100).toInt().coerceIn(0, 100)
                pbCat.progress = percent
                tvPercent.text = "$percent%"

                when (name) {
                    "Makanan" -> ivIcon.setImageResource(R.drawable.ic_food)
                    "Transport" -> ivIcon.setImageResource(R.drawable.ic_transport)
                    "Belanja" -> ivIcon.setImageResource(R.drawable.ic_shopping)
                    "Tagihan" -> ivIcon.setImageResource(R.drawable.ic_bill)
                    "Hiburan" -> ivIcon.setImageResource(R.drawable.ic_entertainment)
                    "Kesehatan" -> ivIcon.setImageResource(R.drawable.ic_health)
                    "Pendidikan" -> ivIcon.setImageResource(R.drawable.ic_education)
                    "Rumah" -> ivIcon.setImageResource(R.drawable.ic_home_nav)
                    "Hadiah" -> ivIcon.setImageResource(R.drawable.ic_gift)
                    "Olahraga" -> ivIcon.setImageResource(R.drawable.ic_sport)
                    "Travel" -> ivIcon.setImageResource(R.drawable.ic_travel)
                    else -> ivIcon.setImageResource(R.drawable.ic_more)
                }

                itemView.setOnClickListener {
                    val intent = Intent(requireContext(), CategoryTransactionActivity::class.java)
                    intent.putExtra("CATEGORY_NAME", name)
                    startActivity(intent)
                }

                binding.categoryContainer.addView(itemView)
            }
        }
    }

    private fun updateGoalsList() {
        binding.goalsContainer.removeAllViews()
        val userId = session.getUserId()
        val goals = db.getAllGoals(userId)
        val formatter = getCurrencyFormatter()

        if (goals.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "Belum ada target tabungan"
                setPadding(0, 20, 0, 20)
                setTextColor(android.graphics.Color.GRAY)
            }
            binding.goalsContainer.addView(emptyText)
            return
        }

        goals.forEach { goal ->
            val itemView = layoutInflater.inflate(R.layout.item_goal, binding.goalsContainer, false)
            val tvName = itemView.findViewById<TextView>(R.id.tvItemGoalName)
            val tvPercent = itemView.findViewById<TextView>(R.id.tvItemGoalPercent)
            val pbGoal = itemView.findViewById<ProgressBar>(R.id.pbItemGoal)
            val tvCurrent = itemView.findViewById<TextView>(R.id.tvItemGoalCurrent)
            val tvTarget = itemView.findViewById<TextView>(R.id.tvItemGoalTarget)
            val tvDeadline = itemView.findViewById<TextView>(R.id.tvItemGoalDeadline)

            val id = goal["id"] as Int
            val name = goal["name"] as String
            val target = goal["target_amount"] as Double
            val current = goal["current_amount"] as Double
            val deadline = goal["deadline"] as String

            val currencyCode = session.getCurrency()
            tvName.text = name
            val percent = if (target > 0) ((current / target) * 100).toInt().coerceIn(0, 100) else 0
            tvPercent.text = "$percent%"
            pbGoal.progress = percent
            tvCurrent.text = "${CurrencyUtils.formatCurrency(current, currencyCode)} terkumpul"
            tvTarget.text = "Target: ${CurrencyUtils.formatCurrency(target, currencyCode)}"
            tvDeadline.text = "Tenggat: $deadline"

            itemView.setOnClickListener {
                val intent = Intent(requireContext(), EditGoalActivity::class.java)
                intent.putExtra("GOAL_ID", id)
                startActivity(intent)
            }
            binding.goalsContainer.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}