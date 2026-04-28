package com.example.solutionchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solutionchallenge.data.models.RunAuditResponse
import com.example.solutionchallenge.data.repository.BiasGuardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultsViewModelSample(
    private val repository: BiasGuardRepository
) : ViewModel() {

    private val _auditResult = MutableStateFlow<RunAuditResponse?>(null)
    val auditResult: StateFlow<RunAuditResponse?> = _auditResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun runAudit(protected: List<String>, prediction: String, target: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.runBiasAudit(protected, prediction, target).collect { result ->
                result.onSuccess {
                    _auditResult.value = it
                }.onFailure { exception ->
                    android.util.Log.e("ResultsViewModel", "Audit failed", exception)
                }
                _isLoading.value = false
            }
        }
    }
}
