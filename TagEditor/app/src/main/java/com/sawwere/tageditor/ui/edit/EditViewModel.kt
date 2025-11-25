package com.sawwere.tageditor.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawwere.tageditor.data.ExifRepository
import com.sawwere.tageditor.ui.SharedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditViewModel(
    private val exifRepository: ExifRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state.asStateFlow()

    fun loadImageData() {
        val imageUri = SharedState.selectedImageUri
        if (imageUri == null) {
            _state.update { it.copy(error = "No image selected") }
            return
        }

        _state.update { it.copy(imageUri = imageUri, isLoading = true) }
        viewModelScope.launch {
            try {
                val exifData = exifRepository.readExifData(android.net.Uri.parse(imageUri))
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
                        error = "Failed to load image data: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateDateTime(dateTime: String) {
        _state.update {
            it.copy(exifData = it.exifData.copy(dateTime = dateTime))
        }
    }

    fun updateLatitude(latitude: String) {
        _state.update {
            it.copy(exifData = it.exifData.copy(latitude = latitude.toDoubleOrNull()))
        }
    }

    fun updateLongitude(longitude: String) {
        _state.update {
            it.copy(exifData = it.exifData.copy(longitude = longitude.toDoubleOrNull()))
        }
    }

    fun updateMake(make: String) {
        _state.update {
            it.copy(exifData = it.exifData.copy(make = make))
        }
    }

    fun updateModel(model: String) {
        _state.update {
            it.copy(exifData = it.exifData.copy(model = model))
        }
    }

    fun setSaveAsCopy(saveAsCopy: Boolean) {
        _state.update { it.copy(saveAsCopy = saveAsCopy) }
    }

    fun saveExifData() {
        val state = _state.value
        if (state.imageUri.isBlank()) {
            _state.update { it.copy(error = "No image selected") }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val success = if (state.saveAsCopy) {
                    val newUri = exifRepository.saveExifDataAsCopy(
                        android.net.Uri.parse(state.imageUri),
                        state.exifData
                    )
                    newUri != null
                } else {
                    exifRepository.saveExifData(
                        android.net.Uri.parse(state.imageUri),
                        state.exifData
                    )
                }

                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = success,
                        error = if (!success) "Failed to save EXIF data" else null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = false,
                        error = "Error saving EXIF data: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearSaveStatus() {
        _state.update { it.copy(saveSuccess = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}