package com.sawwere.healthconnect.ui.insert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawwere.healthconnect.R
import com.sawwere.healthconnect.data.DataType
import com.sawwere.healthconnect.ui.DatePick
import com.sawwere.healthconnect.ui.HealthConnectViewModel
import com.sawwere.healthconnect.ui.toEpochSeconds
import com.sawwere.healthconnect.ui.toLocalDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertDataScreen(
    viewModel: HealthConnectViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedDataType by remember { mutableStateOf(DataType.STEPS) }
    var stepsCount by remember { mutableStateOf("") }
    var weightValue by remember { mutableStateOf("") }
    val selectedTimestamp = remember { mutableStateOf(LocalDate.now().toEpochSeconds()) }

    LaunchedEffect(Unit) {
        viewModel.clearMessages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.add_health_data),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                viewModel.clearMessages()
                onNavigateBack()
            }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.select_data_type), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DataType.values().forEach { type ->
                    FilterChip(
                        selected = selectedDataType == type,
                        onClick = { selectedDataType = type },
                        label = {
                            Text(
                                when(type) {
                                    DataType.STEPS -> stringResource(R.string.steps)
                                    DataType.WEIGHT -> stringResource(R.string.weight)
                                }
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (selectedDataType) {
            DataType.STEPS -> {
                OutlinedTextField(
                    value = stepsCount,
                    onValueChange = { stepsCount = it },
                    label = { Text(stringResource(R.string.steps_count)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.enter_steps)) }
                )
            }
            DataType.WEIGHT -> {
                OutlinedTextField(
                    value = weightValue,
                    onValueChange = { weightValue = it },
                    label = { Text(stringResource(R.string.weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.enter_weight)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.select_date), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            DatePick(
                time = selectedTimestamp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val selectedDate = selectedTimestamp.value.toLocalDate()
                when (selectedDataType) {
                    DataType.STEPS -> {
                        val count = stepsCount.toLongOrNull()
                        if (count != null) {
                            viewModel.insertSteps(count, selectedDate)
                            stepsCount = ""
                        } else {
                            viewModel.setErrorMessage("Пожалуйста, введите корректное число шагов")
                        }
                    }
                    DataType.WEIGHT -> {
                        val weight = weightValue.toDoubleOrNull()
                        if (weight != null) {
                            viewModel.insertWeight(weight, selectedDate)
                            weightValue = ""
                        } else {
                            viewModel.setErrorMessage("Пожалуйста, введите корректный вес (например, 75.5)")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = when (selectedDataType) {
                DataType.STEPS -> stepsCount.isNotBlank() && stepsCount.toLongOrNull() != null
                DataType.WEIGHT -> weightValue.isNotBlank() && weightValue.toDoubleOrNull() != null
            }
        ) {
            Text(stringResource(R.string.save_data), fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.saving_data))
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}