package com.example.voca

import android.content.Intent
import android.os.Bundle
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
import com.example.voca.utils.SessionManager
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
        
        var selectedType = session.getBudgetType()
        etAmount.setText(session.getBudgetTarget().toLong().toString())

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

        AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val amount = etAmount.text.toString().toFloatOrNull() ?: 0f
                session.setBudgetTarget(amount)
                session.setBudgetType(selectedType)
                updateUI()
            }
            .setNegativeButton("Batal", null)
            .show()
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

    private fun updateUI() {
        val transactions = db.getAllTransactions()
        
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        val now = Calendar.getInstance()
        
        val budgetType = session.getBudgetType()
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
        val currentPeriodExpense = filteredTransactions.filter { it["type"] == "expense" }.sumOf { it["amount"] as Double }
        val remainingBudget = (budgetTarget - currentPeriodExpense).coerceAtLeast(0.0)

        val formatter = getCurrencyFormatter()
        binding.tvMonth.text = "${monthFormat.format(Date())} ($budgetType)"
        binding.tvRemainingBudget.text = formatter.format(remainingBudget).replace("Rp", "Rp ")
        
        val percentUsed = if (budgetTarget > 0) ((currentPeriodExpense / budgetTarget) * 100).toInt().coerceIn(0, 100) else 0
        binding.pbBudget.progress = percentUsed
        
        val expenseStr = formatter.format(currentPeriodExpense).replace("Rp", "Rp ")
        val targetStr = formatter.format(budgetTarget).replace("Rp", "Rp ")
        binding.tvBudgetStatus.text = "$percentUsed% terpakai ($expenseStr dari target $targetStr)"

        updateCategoryList(categoryExpenses)
        updateGoalsList()
    }

    private fun updateCategoryList(categoryExpenses: Map<String, Double>) {
        binding.categoryContainer.removeAllViews()
        val categoryTargets = mapOf("Makanan" to 3000000.0, "Transport" to 1500000.0, "Belanja" to 4000000.0, "Lainnya" to 3500000.0)
        val sortedCategories = categoryTargets.keys.sortedByDescending { categoryExpenses[it] ?: 0.0 }

        sortedCategories.forEach { name ->
            val expense = categoryExpenses[name] ?: 0.0
            val target = categoryTargets[name] ?: 1.0
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
                "Makanan" -> {
                    ivIcon.setImageResource(R.drawable.ic_food)
                    cardIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#E2F2FF"))
                }
                "Transport" -> ivIcon.setImageResource(R.drawable.ic_transport)
                "Belanja" -> ivIcon.setImageResource(R.drawable.ic_shopping)
                else -> ivIcon.setImageResource(R.drawable.ic_more)
            }
            binding.categoryContainer.addView(itemView)
        }
    }

    private fun updateGoalsList() {
        binding.goalsContainer.removeAllViews()
        val goals = db.getAllGoals()
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

            tvName.text = name
            val percent = if (target > 0) ((current / target) * 100).toInt().coerceIn(0, 100) else 0
            tvPercent.text = "$percent%"
            pbGoal.progress = percent
            tvCurrent.text = "${formatter.format(current).replace("Rp", "Rp ")} terkumpul"
            tvTarget.text = "Target: ${formatter.format(target).replace("Rp", "Rp ")}"
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