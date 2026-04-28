package com.example.solutionchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solutionchallenge.data.models.UploadResponse
import com.example.solutionchallenge.data.repository.BiasGuardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModelSample(
    private val repository: BiasGuardRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadResponse?>(null)
    val uploadState: StateFlow<UploadResponse?> = _uploadState.asStateFlow()

    fun uploadDataset(file: File) {
        viewModelScope.launch {
            repository.uploadData(file).collect { result ->
                result.onSuccess {
                    _uploadState.value = it
                }.onFailure { exception ->
                    android.util.Log.e("UploadViewModel", "Upload Network Call Failed:", exception)
                }
            }
        }
    }
}
