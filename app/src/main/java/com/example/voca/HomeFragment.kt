package com.example.voca

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.voca.database.DatabaseHelper
import com.example.voca.databinding.FragmentHomeBinding
import com.example.voca.utils.CurrencyUtils
import com.example.voca.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())
        session = SessionManager(requireContext())

        val fullName = session.getUserName() ?: "User"
        val firstName = fullName.split("@")[0].split(" ")[0]
        binding.tvWelcome.text = "Halo, $firstName"
        
        setupFilters()
        updateUI()
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            updateUI()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        if (_binding == null) return
        val userId = session.getUserId()
        val allTransactions = db.getAllTransactions(userId)
        
        val selectedFilter = when (binding.chipGroupFilters.checkedChipId) {
            R.id.chipDaily -> "Harian"
            R.id.chipWeekly -> "Mingguan"
            R.id.chipMonthly -> "Bulanan"
            R.id.chipYearly -> "Tahunan"
            else -> "Semua"
        }

        val filteredTransactions = if (selectedFilter == "Semua") {
            allTransactions
        } else {
            filterTransactions(allTransactions, selectedFilter)
        }

        var totalIncome = 0.0
        var totalExpense = 0.0

        for (t in filteredTransactions) {
            val amount = (t["amount"] as? Double) ?: 0.0
            if (t["type"] == "income") totalIncome += amount else totalExpense += amount
        }

        val netWorth = totalIncome - totalExpense
        val currencyCode = session.getCurrency()
        
        // Update Summary Card based on filtered data
        binding.tvTotalBalance.text = CurrencyUtils.formatCurrency(netWorth, currencyCode)
        binding.tvIncome.text = CurrencyUtils.formatCurrency(totalIncome, currencyCode)
        binding.tvExpense.text = CurrencyUtils.formatCurrency(totalExpense, currencyCode)

        // Show label for filtered period
        binding.tvRecentTransactions.text = if (selectedFilter == "Semua") "Transaksi Terbaru" else "Transaksi $selectedFilter"

        // Send only filtered transactions to the list view!
        updateRecentTransactions(filteredTransactions)
    }

    private fun filterTransactions(transactions: List<Map<String, Any>>, filter: String): List<Map<String, Any>> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val now = calendar.time

        return transactions.filter {
            val tDate = it["date"]?.toString() ?: ""
            try {
                val date = dateFormat.parse(tDate) ?: return@filter false
                val calT = Calendar.getInstance().apply { time = date }
                val calNow = Calendar.getInstance().apply { time = now }

                when (filter) {
                    "Harian" -> {
                        calT.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                        calT.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
                    }
                    "Mingguan" -> {
                        // In the same week of year
                        calT.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                        calT.get(Calendar.WEEK_OF_YEAR) == calNow.get(Calendar.WEEK_OF_YEAR)
                    }
                    "Bulanan" -> {
                        calT.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                        calT.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
                    }
                    "Tahunan" -> {
                        calT.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
                    }
                    else -> true
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun updateRecentTransactions(transactions: List<Map<String, Any>>) {
        val container = binding.containerRecentTransactions
        container.removeAllViews()

        if (transactions.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "Belum ada transaksi"
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white))
            }
            container.addView(emptyText)
            return
        }

        val currencyCode = session.getCurrency()
        transactions.take(10).forEach { t ->
            val itemView = layoutInflater.inflate(R.layout.item_transaction_simple, container, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTransTitle)
            val tvDate = itemView.findViewById<TextView>(R.id.tvTransDate)
            val tvAmount = itemView.findViewById<TextView>(R.id.tvTransAmount)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivTransIcon)

            val title = t["title"]?.toString() ?: "-"
            val amount = (t["amount"] as? Double) ?: 0.0
            val type = t["type"]?.toString() ?: "expense"
            val category = t["category"]?.toString() ?: "Lainnya"
            val date = t["date"]?.toString() ?: "-"
            val id = (t["id"] as? Int) ?: -1

            tvTitle.text = title
            tvDate.text = date

            if (type == "income") {
                tvAmount.text = "+${CurrencyUtils.formatCurrency(amount, currencyCode)}"
                tvAmount.setTextColor(android.graphics.Color.parseColor("#48BB78"))
                ivIcon.setImageResource(R.drawable.ic_income_default)
                ivIcon.setBackgroundResource(R.drawable.circle_green_bg)
            } else {
                tvAmount.text = "-${CurrencyUtils.formatCurrency(amount, currencyCode)}"
                tvAmount.setTextColor(android.graphics.Color.parseColor("#F56565"))
                
                when (category) {
                    "Makanan" -> ivIcon.setImageResource(R.drawable.ic_food)
                    "Transport" -> ivIcon.setImageResource(R.drawable.ic_transport)
                    "Belanja" -> ivIcon.setImageResource(R.drawable.ic_shopping)
                    else -> ivIcon.setImageResource(R.drawable.ic_more)
                }
                ivIcon.setBackgroundResource(R.drawable.circle_light_bg)
                ivIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#003285"))
            }

            itemView.setOnClickListener {
                val intent = Intent(requireContext(), DetailTransactionActivity::class.java)
                intent.putExtra("TRANSACTION_ID", id)
                startActivity(intent)
            }

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}