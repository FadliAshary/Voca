package com.example.voca

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.voca.adapter.TipsAdapter
import com.example.voca.api.ApiService
import com.example.voca.databinding.ActivityTipsBinding
import com.example.voca.repository.FinanceRepository
import com.example.voca.viewmodel.FinanceViewModel
import com.example.voca.viewmodel.FinanceViewModelFactory

class TipsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTipsBinding
    private lateinit var viewModel: FinanceViewModel
    private lateinit var adapter: TipsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiService.create()
        val repository = FinanceRepository(apiService)
        val factory = FinanceViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FinanceViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        viewModel.fetchTips()
    }

    private fun setupRecyclerView() {
        adapter = TipsAdapter(emptyList())
        binding.rvTips.layoutManager = LinearLayoutManager(this)
        binding.rvTips.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.tips.observe(this) { tips ->
            adapter.updateData(tips)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }
}