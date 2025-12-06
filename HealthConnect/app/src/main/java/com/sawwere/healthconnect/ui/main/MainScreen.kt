package com.sawwere.healthconnect.ui.main

import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.sawwere.healthconnect.MainActivity
import com.sawwere.healthconnect.data.DataType
import com.sawwere.healthconnect.data.HealthConnectRepository
import com.sawwere.healthconnect.data.HealthData
import com.sawwere.healthconnect.ui.DateRangeFilter
import com.sawwere.healthconnect.ui.HealthConnectUiState
import com.sawwere.healthconnect.ui.HealthConnectViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: HealthConnectViewModel,
    onNavigateToInsert: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                text = "Health Data",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                Button(
                    onClick = { viewModel.checkPermissions() },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Refresh")
                }

                if (uiState.permissionsGranted) {
                    Button(
                        onClick = onNavigateToInsert,
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Data")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!uiState.permissionsGranted) {
            SimplePermissionScreen(
                isLoading = uiState.isLoading,
                onRequestPermissions = onRequestPermissions,
                errorMessage = uiState.errorMessage
            )
            return
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Data Type:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DataType.values().forEach { dataType ->
                    FilterChip(
                        selected = uiState.selectedDataType == dataType,
                        onClick = { viewModel.updateDataType(dataType) },
                        label = { Text(dataType.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DateRangeFilter(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onDateRangeChanged = { start, end ->
                viewModel.updateDateRange(start, end)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.selectedDataType) {
            DataType.STEPS -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Total Steps: ${uiState.totalSteps}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            DataType.WEIGHT -> {
                uiState.averageWeight?.let { avgWeight ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Average Weight: ${String.format("%.1f", avgWeight)} kg",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.dataList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No data available",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Select different date range or add new data",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToInsert,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add First Data Entry")
                    }
                }
            }
        } else {
            DataList(
                dataList = uiState.dataList,
                onDelete = { record ->
                    viewModel.deleteRecord(record)
                }
            )
        }

        uiState.successMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SimplePermissionScreen(
    isLoading: Boolean,
    onRequestPermissions: () -> Unit,
    errorMessage: String?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Health Connect Permissions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "This app needs permission to:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Read your step count")
                Text("• Write step data")
                Text("• Read your weight")
                Text("• Write weight data")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Checking permissions...")
        } else {
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Grant Permissions")
            }
        }

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    uiState: HealthConnectUiState,
    viewModel: HealthConnectViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Data Type:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DataType.values().forEach { dataType ->
                    FilterChip(
                        selected = uiState.selectedDataType == dataType,
                        onClick = { viewModel.updateDataType(dataType) },
                        label = { Text(dataType.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DateRangeFilter(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onDateRangeChanged = { start, end ->
                viewModel.updateDateRange(start, end)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState.selectedDataType) {
            DataType.STEPS -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Total Steps: ${uiState.totalSteps}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            DataType.WEIGHT -> {
                uiState.averageWeight?.let { avgWeight ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Average Weight: ${String.format("%.1f", avgWeight)} kg",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.dataList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No data available",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Select different date range or add new data",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            DataList(
                dataList = uiState.dataList,
                onDelete = { record ->
                    viewModel.deleteRecord(record)
                }
            )
        }

        uiState.successMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun DataList(
    dataList: List<HealthData>,
    onDelete: (HealthData) -> Unit
) {
    LazyColumn {
        items(dataList) { data ->
            when (data) {
                is HealthData.StepData -> {
                    StepItem(
                        stepData = data,
                        onDelete = { onDelete(data) }
                    )
                }
                is HealthData.WeightData -> {
                    WeightItem(
                        weightData = data,
                        onDelete = { onDelete(data) }
                    )
                }
            }
            Divider()
        }
    }
}

@Composable
fun StepItem(
    stepData: HealthData.StepData,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${stepData.count} steps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stepData.date.format(DateTimeFormatter.ISO_DATE),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun WeightItem(
    weightData: HealthData.WeightData,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${String.format("%.1f", weightData.weight)} kg",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = weightData.date.format(DateTimeFormatter.ISO_DATE),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}