package com.sawwere.tageditor.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavHostController,
    editViewModel: EditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by editViewModel.state.collectAsStateWithLifecycle()

    // Загружаем данные изображения при входе на экран
    LaunchedEffect(Unit) {
        editViewModel.loadImageData()
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess == true) {
            delay(1000)
            navController.popBackStack()
        }
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.imageUri.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Изображение не выбрано",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "Вернитесь на главный экран и выберите изображение",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("Вернуться к выбору")
                    }
                }
                return@Column
            }

            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Загрузка...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
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
                    Text("Сохранение...")
                }
            }

            state.saveSuccess?.let { success ->
                AlertDialog(
                    onDismissRequest = { editViewModel.clearSaveStatus() },
                    title = { Text(if (success) "Успех" else "Ошибка") },
                    text = {
                        Text(
                            if (success)
                                "Изменения успешно сохранены!"
                            else
                                state.error ?: "Ошибка при сохранении"
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            editViewModel.clearSaveStatus()
                            if (success) {
                                navController.popBackStack()
                            }
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

            // Показываем форму редактирования только если не загружается и есть данные
            if (!state.isLoading && state.imageUri.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.saveAsCopy,
                            onCheckedChange = {
                                editViewModel.setSaveAsCopy(it)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Сохранить как копию",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Рекомендуется для сохранения исходного файла",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.exifData.dateTime,
                    onValueChange = {
                        editViewModel.updateDateTime(it)
                    },
                    label = { Text("Дата создания") },
                    placeholder = { Text("YYYY:MM:DD HH:MM:SS") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.exifData.latitude?.toString() ?: "",
                        onValueChange = {
                            editViewModel.updateLatitude(it)
                        },
                        label = { Text("Широта") },
                        placeholder = { Text("00.000000") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = state.exifData.longitude?.toString() ?: "",
                        onValueChange = {
                            editViewModel.updateLongitude(it)
                        },
                        label = { Text("Долгота") },
                        placeholder = { Text("00.000000") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.exifData.make,
                    onValueChange = {
                        editViewModel.updateMake(it)
                    },
                    label = { Text("Устройство создания") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = state.exifData.model,
                    onValueChange = {
                        editViewModel.updateModel(it)
                    },
                    label = { Text("Модель устройства") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                            editViewModel.saveExifData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isSaving
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}