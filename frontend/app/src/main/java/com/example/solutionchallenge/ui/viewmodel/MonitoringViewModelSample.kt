package com.example.solutionchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solutionchallenge.data.models.MonitorResponse
import com.example.solutionchallenge.data.repository.BiasGuardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MonitoringViewModelSample(
    private val repository: BiasGuardRepository
) : ViewModel() {

    private val _monitorData = MutableStateFlow<MonitorResponse?>(null)
    val monitorData: StateFlow<MonitorResponse?> = _monitorData.asStateFlow()

    fun fetchMonitoringData() {
        viewModelScope.launch {
            repository.monitorBias().collect { result ->
                result.onSuccess {
                    _monitorData.value = it
                }.onFailure {
                    // Handle network error state
                }
            }
        }
    }
}
