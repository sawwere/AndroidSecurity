package com.sawwere.healthconnect.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawwere.healthconnect.data.DataType
import com.sawwere.healthconnect.data.HealthConnectProvider
import com.sawwere.healthconnect.data.HealthData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class HealthConnectViewModel(
    private val provider: HealthConnectProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val hasPermissions = provider.checkPermissions()

                _uiState.update {
                    it.copy(
                        permissionsGranted = hasPermissions,
                        isLoading = false
                    )
                }

                if (hasPermissions) {
                    loadData()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to check permissions: ${e.message}"
                    )
                }
            }
        }
    }

    fun onPermissionsGranted() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    permissionsGranted = true,
                    successMessage = "Permissions granted!"
                )
            }
            provider.permissionsGranted = true
            loadData()
        }
    }

    fun onPermissionsDenied() {
        _uiState.update {
            it.copy(
                permissionsGranted = false,
                errorMessage = "Please grant all permissions to use the app"
            )
        }
        provider.permissionsGranted = false
    }

    fun loadData(
        dataType: DataType = DataType.STEPS,
        startDate: LocalDate = LocalDate.now().minusDays(7),
        endDate: LocalDate = LocalDate.now()
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                if (!provider.permissionsGranted) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Permissions not granted"
                        )
                    }
                    return@launch
                }

                when (dataType) {
                    DataType.STEPS -> loadStepsData(startDate, endDate)
                    DataType.WEIGHT -> loadWeightData(startDate, endDate)
                }

                _uiState.update {
                    it.copy(
                        selectedDataType = dataType,
                        startDate = startDate,
                        endDate = endDate,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadStepsData(startDate: LocalDate, endDate: LocalDate) {
        val stepsRecords = provider.repository.readStepsByDateRange(startDate, endDate)
        val stepsData = stepsRecords.map { record ->
            HealthData.StepData(
                id = record.metadata.id,
                count = record.count,
                date = record.startTime.atZone(ZoneId.systemDefault()).toLocalDate(),
                startTime = record.startTime,
                endTime = record.endTime
            )
        }

        val totalSteps = provider.repository.aggregateStepsByDateRange(startDate, endDate)

        _uiState.update {
            it.copy(
                dataList = stepsData,
                totalSteps = totalSteps
            )
        }
    }

    private suspend fun loadWeightData(startDate: LocalDate, endDate: LocalDate) {
        val weightRecords = provider.repository.readWeightByDateRange(startDate, endDate)
        val weightData = weightRecords.map { record ->
            HealthData.WeightData(
                id = record.metadata.id,
                weight = record.weight.inKilograms,
                date = record.time.atZone(ZoneId.systemDefault()).toLocalDate(),
                time = record.time
            )
        }

        val averageWeight = if (weightRecords.isNotEmpty()) {
            weightRecords.map { it.weight.inKilograms }.average()
        } else null

        _uiState.update {
            it.copy(
                dataList = weightData,
                averageWeight = averageWeight
            )
        }
    }

    fun insertSteps(count: Long, date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = provider.repository.insertSteps(count, date)
                if (success) {
                    loadData()
                    _uiState.update { it.copy(
                        successMessage = "Steps added successfully",
                        isLoading = false
                    )}
                } else {
                    _uiState.update { it.copy(
                        errorMessage = "Failed to add steps",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Error: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    fun insertWeight(weightKg: Double, date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = provider.repository.insertWeight(weightKg, date)
                if (success) {
                    loadData()
                    _uiState.update { it.copy(
                        successMessage = "Weight added successfully",
                        isLoading = false
                    )}
                } else {
                    _uiState.update { it.copy(
                        errorMessage = "Failed to add weight",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Error: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    fun updateDateRange(startDate: LocalDate, endDate: LocalDate) {
        loadData(_uiState.value.selectedDataType, startDate, endDate)
    }

    fun updateDataType(dataType: DataType) {
        loadData(dataType, _uiState.value.startDate, _uiState.value.endDate)
    }

    fun deleteRecord(record: HealthData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val success = when (record) {
                    is HealthData.StepData -> {
                        provider.repository.deleteStepsById(record.id)
                    }
                    is HealthData.WeightData -> {
                        provider.repository.deleteWeightById(record.id)
                    }
                }

                if (success) {
                    loadData()
                    _uiState.update { it.copy(
                        successMessage = "${record::class.simpleName} deleted successfully",
                        isLoading = false
                    )}
                } else {
                    _uiState.update { it.copy(
                        errorMessage = "Failed to delete record",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Error: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    fun setErrorMessage(message: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    errorMessage = message,
                    isLoading = false
                )
            }
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }
}

data class HealthConnectUiState(
    val permissionsGranted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val dataList: List<HealthData> = emptyList(),
    val selectedDataType: DataType = DataType.STEPS,
    val startDate: LocalDate = LocalDate.now().minusDays(7),
    val endDate: LocalDate = LocalDate.now(),
    val totalSteps: Long = 0,
    val averageWeight: Double? = null,
)