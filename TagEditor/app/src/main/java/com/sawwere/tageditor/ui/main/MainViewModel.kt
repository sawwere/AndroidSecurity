package com.sawwere.tageditor.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawwere.tageditor.data.ExifRepository
import com.sawwere.tageditor.ui.SharedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val exifRepository: ExifRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun selectImage(uri: String) {
        SharedState.selectedImageUri = uri
        _state.update { it.copy(selectedImageUri = uri, isLoading = true) }
        loadExifData(uri)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun loadExifData(uri: String) {
        viewModelScope.launch {
            try {
                val exifData = exifRepository.readExifData(android.net.Uri.parse(uri))
                _state.update {
                    it.copy(
                        exifData = exifData,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load EXIF data: ${e.message}"
                    )
                }
            }
        }
    }
}