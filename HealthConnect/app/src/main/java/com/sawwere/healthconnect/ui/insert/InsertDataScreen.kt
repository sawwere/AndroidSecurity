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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawwere.healthconnect.data.DataType
import com.sawwere.healthconnect.ui.DatePick
import com.sawwere.healthconnect.ui.HealthConnectViewModel
import com.sawwere.healthconnect.ui.toEpochSeconds
import com.sawwere.healthconnect.ui.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("added successfully") == true) {
            onNavigateBack()
        }
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
                text = "Add Health Data",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select data type:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DataType.values().forEach { type ->
                    FilterChip(
                        selected = selectedDataType == type,
                        onClick = { selectedDataType = type },
                        label = { Text(type.name) }
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
                    label = { Text("Steps count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter number of steps") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Example: 10000 steps for a full day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            DataType.WEIGHT -> {
                OutlinedTextField(
                    value = weightValue,
                    onValueChange = { weightValue = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter weight in kilograms") }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Example: 75.5 for 75.5 kilograms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Date:", style = MaterialTheme.typography.labelLarge)
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
                        } else {
                            // Показываем ошибку, если ввод некорректный
                            //viewModel.setErrorMessage("Please enter a valid number for steps")
                        }
                    }
                    DataType.WEIGHT -> {
                        val weight = weightValue.toDoubleOrNull()
                        if (weight != null) {
                            viewModel.insertWeight(weight, selectedDate)
                        } else {
                            println("Please enter a valid weight (e.g., 75.5)")
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
            Text("Save Data", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving data...")
            }
        }

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        uiState.successMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Success",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    return date.format(formatter)
}