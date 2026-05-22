package com.example.voca.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.voca.model.FinanceTip
import com.example.voca.repository.FinanceRepository

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _tips = MutableLiveData<List<FinanceTip>>()
    val tips: LiveData<List<FinanceTip>> get() = _tips

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchTips() {
        _isLoading.value = true
        repository.getFinanceTips { data, t ->
            _isLoading.value = false
            if (t != null) {
                _error.value = t.message
            } else {
                _tips.value = data ?: emptyList()
            }
        }
    }
}