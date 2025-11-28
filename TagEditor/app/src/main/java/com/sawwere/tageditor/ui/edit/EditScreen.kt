package com.sawwere.tageditor.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.sawwere.tageditor.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavHostController,
    imageUri: String,
    editViewModel: EditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by editViewModel.state.collectAsStateWithLifecycle()
    val isFormValid = editViewModel.validateForm()
    val isDateTimeValid = editViewModel.validateDateTime()
    val areCoordinatesValid = editViewModel.validateCoordinates()
    val dateTimeError = editViewModel.getDateTimeValidationError()
    val coordinatesError = editViewModel.getCoordinatesValidationError()
    val isCoordinatePartiallyFilled = editViewModel.isCoordinatePartiallyFilled()

    LaunchedEffect(imageUri) {
        editViewModel.loadImageData(imageUri)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Редактор EXIF",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }

            if (state.isSaving) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                    Text("Сохранение EXIF данных...")
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Изменение EXIF данных изображения",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedTextField(
                value = state.exifData.dateTime,
                onValueChange = { editViewModel.updateDateTime(it) },
                label = { Text("Дата создания (YYYY:MM:DD HH:MM:SS)") },
                placeholder = { Text("2023:12:01 15:30:00") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.exifData.dateTime.isNotEmpty() && !isDateTimeValid,
                supportingText = {
                    if (state.exifData.dateTime.isNotEmpty() && !isDateTimeValid) {
                        Text(dateTimeError ?: "Неверный формат даты")
                    } else {
                        Text("Формат: ГГГГ:ММ:ДД ЧЧ:ММ:СС")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.exifData.latitude?.toString() ?: "",
                    onValueChange = { editViewModel.updateLatitude(it) },
                    label = { Text("Широта") },
                    placeholder = { Text("55.7558") },
                    modifier = Modifier.weight(1f),
                    isError = state.exifData.latitude != null &&
                            !editViewModel.isCoordinateValid(state.exifData.latitude, -90.0, 90.0),
                    supportingText = {
                        if (state.exifData.latitude != null &&
                            !editViewModel.isCoordinateValid(state.exifData.latitude, -90.0, 90.0)) {
                            Text("Широта должна быть от -90 до 90")
                        } else {
                            Text("От -90 до 90")
                        }
                    }
                )

                OutlinedTextField(
                    value = state.exifData.longitude?.toString() ?: "",
                    onValueChange = { editViewModel.updateLongitude(it) },
                    label = { Text("Долгота") },
                    placeholder = { Text("37.6173") },
                    modifier = Modifier.weight(1f),
                    isError = state.exifData.longitude != null &&
                            !editViewModel.isCoordinateValid(state.exifData.longitude, -180.0, 180.0),
                    supportingText = {
                        if (state.exifData.longitude != null &&
                            !editViewModel.isCoordinateValid(state.exifData.longitude, -180.0, 180.0)) {
                            Text("Долгота должна быть от -180 до 180")
                        } else {
                            Text("От -180 до 180")
                        }
                    }
                )
            }

            if (!areCoordinatesValid && (state.exifData.latitude != null || state.exifData.longitude != null)) {
                Text(
                    coordinatesError ?: "Исправьте ошибки в координатах",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }

            if (isCoordinatePartiallyFilled) {
                Text(
                    "Заполните оба поля координат",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.exifData.make,
                onValueChange = { editViewModel.updateMake(it) },
                label = { Text("Устройство") },
                placeholder = { Text("Samsung") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.exifData.model,
                onValueChange = { editViewModel.updateModel(it) },
                label = { Text("Модель") },
                placeholder = { Text("Galaxy S23") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Отмена")
                }

                Button(
                    onClick = {
                        editViewModel.saveExifData(imageUri)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving && !state.isLoading && isFormValid
                ) {
                    Text("Сохранить")
                }
            }
        }
    }

    if (state.saveSuccess == true) {
        AlertDialog(
            onDismissRequest = {
                editViewModel.clearSaveStatus()
                navController.popBackStack()
            },
            title = { Text("Успех") },
            text = {
                Text("EXIF данные успешно сохранены в исходном изображении!")
            },
            confirmButton = {
                Button(onClick = {
                    editViewModel.clearSaveStatus()
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = { editViewModel.clearError() },
            title = { Text("Ошибка") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { editViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}