package com.sawwere.tageditor.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawwere.tageditor.data.ExifData
import com.sawwere.tageditor.data.ExifDataValidator
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
    private val validator = ExifDataValidator()

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
            it.copy(exifData = it.exifData.copy(latitude = parseCoordinate(latitude)))
        }
    }

    fun updateLongitude(longitude: String) {
        _state.update {
            it.copy(exifData = it.exifData.copy(longitude = parseCoordinate(longitude)))
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

    private fun parseCoordinate(coordinate: String): Double? {
        return try {
            coordinate.trim().takeIf { it.isNotEmpty() }?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    fun validateForm(): Boolean {
        return validator.validateForm(_state.value.exifData).isValid
    }

    fun validateCoordinates(): Boolean {
        return validator.validateCoordinates(_state.value.exifData).isValid
    }

    fun validateDateTime(): Boolean {
        return validator.validateDateTime(_state.value.exifData.dateTime).isValid
    }

    fun getDateTimeValidationError(): String? {
        return validator.validateDateTime(_state.value.exifData.dateTime).errorMessage
    }

    fun getCoordinatesValidationError(): String? {
        return validator.validateCoordinates(_state.value.exifData).errorMessage
    }

    fun isCoordinateValid(coord: Double?, min: Double, max: Double): Boolean {
        return validator.isCoordinateValid(coord, min, max)
    }

    fun isCoordinatePartiallyFilled(): Boolean {
        return validator.isCoordinatePartiallyFilled(_state.value.exifData)
    }

    fun saveExifData(originalImageUri: String) {
        if (!validateForm()) {
            _state.update {
                it.copy(error = "Пожалуйста, исправьте ошибки в форме перед сохранением")
            }
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val success = exifRepository.saveExifData(
                    android.net.Uri.parse(originalImageUri),
                    _state.value.exifData
                )

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

    fun setInitialData(exifData: ExifData) {
        _state.update {
            it.copy(
                exifData = exifData,
                isLoading = false
            )
        }
    }
}