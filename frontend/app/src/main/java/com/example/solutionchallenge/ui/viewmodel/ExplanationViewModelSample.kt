package com.example.solutionchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solutionchallenge.data.models.ExplanationResponse
import com.example.solutionchallenge.data.repository.BiasGuardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExplanationViewModelSample(
    private val repository: BiasGuardRepository
) : ViewModel() {

    private val _explanationResult = MutableStateFlow<ExplanationResponse?>(null)
    val explanationResult: StateFlow<ExplanationResponse?> = _explanationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchExplanation() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getExplanations().collect { result ->
                result.onSuccess {
                    _explanationResult.value = it
                }.onFailure { exception ->
                    android.util.Log.e("ExplanationViewModel", "Explanation fetch failed", exception)
                }
                _isLoading.value = false
            }
        }
    }
}
