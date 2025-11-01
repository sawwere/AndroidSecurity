package com.example.inventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.AppSettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: AppSettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                hideSensitiveData = settingsManager.hideSensitiveData,
                disableSharing = settingsManager.disableSharing,
                useDefaultQuantity = settingsManager.useDefaultQuantity,
                defaultQuantity = settingsManager.defaultQuantity
            )
        }
    }

    fun updateHideSensitiveData(value: Boolean) {
        settingsManager.hideSensitiveData = value
        _uiState.value = _uiState.value.copy(hideSensitiveData = value)
    }

    fun updateDisableSharing(value: Boolean) {
        settingsManager.disableSharing = value
        _uiState.value = _uiState.value.copy(disableSharing = value)
    }

    fun updateUseDefaultQuantity(value: Boolean) {
        settingsManager.useDefaultQuantity = value
        _uiState.value = _uiState.value.copy(useDefaultQuantity = value)
    }

    fun updateDefaultQuantity(value: String) {
        settingsManager.defaultQuantity = value
        _uiState.value = _uiState.value.copy(defaultQuantity = value)
    }
}

data class SettingsUiState(
    val hideSensitiveData: Boolean = false,
    val disableSharing: Boolean = false,
    val useDefaultQuantity: Boolean = false,
    val defaultQuantity: String = "1"
)