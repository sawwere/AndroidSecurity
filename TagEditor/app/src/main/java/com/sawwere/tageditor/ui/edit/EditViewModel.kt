package com.sawwere.tageditor.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawwere.tageditor.data.ExifRepository
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

    fun loadImageData(imageUri: String) {
        _state.update { it.copy(isLoading = true) }
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

    fun saveExifData(originalImageUri: String) {
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val newUri = exifRepository.saveExifDataAsCopy(
                    android.net.Uri.parse(originalImageUri),
                    _state.value.exifData
                )

                val success = newUri != null
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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSaveStatus() {
        _state.update { it.copy(saveSuccess = null) }
    }
}