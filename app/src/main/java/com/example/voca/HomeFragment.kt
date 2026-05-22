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
import com.example.voca.utils.SessionManager
import java.text.NumberFormat
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
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val transactions = db.getAllTransactions()
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (t in transactions) {
            val amount = t["amount"] as Double
            if (t["type"] == "income") totalIncome += amount else totalExpense += amount
        }

        val netWorth = totalIncome - totalExpense
        
        val formatter = getCurrencyFormatter()
        
        // Total Saldo = Net worth (overall money)
        binding.tvTotalBalance.text = formatter.format(netWorth).replace("Rp", "Rp ")
        
        binding.tvIncome.text = formatter.format(totalIncome).replace("Rp", "Rp ")
        binding.tvExpense.text = formatter.format(totalExpense).replace("Rp", "Rp ")

        updateRecentTransactions(transactions)
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

    private fun updateRecentTransactions(transactions: List<Map<String, Any>>) {
        val container = binding.cardRecentTransactions.getChildAt(0) as LinearLayout
        container.removeAllViews()

        if (transactions.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "Belum ada transaksi"
                gravity = android.view.Gravity.CENTER
                setPadding(0, 40, 0, 40)
                setTextColor(android.graphics.Color.WHITE)
            }
            container.addView(emptyText)
            return
        }

        val formatter = getCurrencyFormatter()
        transactions.take(10).forEach { t ->
            val itemView = layoutInflater.inflate(R.layout.item_transaction_simple, container, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTransTitle)
            val tvDate = itemView.findViewById<TextView>(R.id.tvTransDate)
            val tvAmount = itemView.findViewById<TextView>(R.id.tvTransAmount)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivTransIcon)

            val title = t["title"] as String
            val amount = t["amount"] as Double
            val type = t["type"] as String
            val category = t["category"] as? String ?: "Lainnya"
            val date = t["date"] as String
            val id = t["id"] as Int

            tvTitle.text = title
            tvDate.text = date

            if (type == "income") {
                tvAmount.text = "+${formatter.format(amount).replace("Rp", "Rp ")}"
                tvAmount.setTextColor(android.graphics.Color.parseColor("#48BB78"))
                ivIcon.setImageResource(R.drawable.ic_income_default)
                ivIcon.setBackgroundResource(R.drawable.circle_green_bg)
            } else {
                tvAmount.text = "-${formatter.format(amount).replace("Rp", "Rp ")}"
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