package com.example.solutionchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solutionchallenge.data.models.AuditResult
import com.example.solutionchallenge.data.repository.BiasGuardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BiasUiState {
    object Idle : BiasUiState()
    object Loading : BiasUiState()
    data class Success(val result: AuditResult) : BiasUiState()
    data class Error(val message: String) : BiasUiState()
}

class BiasViewModel(
    private val repository: BiasGuardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BiasUiState>(BiasUiState.Idle)
    val uiState: StateFlow<BiasUiState> = _uiState

    fun runAudit(dataset: String, targetColumn: String, protectedAttribute: String) {
        _uiState.value = BiasUiState.Loading
        viewModelScope.launch {
            repository.runAudit(dataset, targetColumn, protectedAttribute).collect { result ->
                result.fold(
                    onSuccess = { _uiState.value = BiasUiState.Success(it) },
                    onFailure = { _uiState.value = BiasUiState.Error(it.message ?: "Unknown error occurred") }
                )
            }
        }
    }
}
