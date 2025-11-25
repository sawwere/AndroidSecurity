package com.sawwere.tageditor.ui.edit

import com.sawwere.tageditor.data.ExifData
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sawwere.tageditor.data.ExifUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    navController: NavHostController,
    imageUri: String
) {
    val context = LocalContext.current
    var exifData by remember { mutableStateOf(ExifData()) }
    var saveSuccess by remember { mutableStateOf<Boolean?>(null) }
    var saveAsCopy by remember { mutableStateOf(true) }

    LaunchedEffect(imageUri) {
        exifData = ExifUtils.readExifData(context, Uri.parse(imageUri))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактор EXIF тегов") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("Назад")
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
                        checked = saveAsCopy,
                        onCheckedChange = { saveAsCopy = it }
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

            saveSuccess?.let { success ->
                AlertDialog(
                    onDismissRequest = { saveSuccess = null },
                    title = { Text(if (success) "Успех" else "Ошибка") },
                    text = {
                        Text(
                            if (success)
                                "Изменения успешно сохранены!"
                            else
                                "Ошибка при сохранении. Попробуйте сохранить как копию."
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            saveSuccess = null
                            if (success) {
                                navController.popBackStack()
                            }
                        }) {
                            Text("OK")
                        }
                    }
                )
            }

            OutlinedTextField(
                value = exifData.dateTime,
                onValueChange = { exifData = exifData.copy(dateTime = it) },
                label = { Text("Дата создания (формат: YYYY:MM:DD HH:MM:SS)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = exifData.latitude?.toString() ?: "",
                    onValueChange = {
                        exifData = exifData.copy(latitude = it.toDoubleOrNull())
                    },
                    label = { Text("Широта") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = exifData.longitude?.toString() ?: "",
                    onValueChange = {
                        exifData = exifData.copy(longitude = it.toDoubleOrNull())
                    },
                    label = { Text("Долгота") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = exifData.make,
                onValueChange = { exifData = exifData.copy(make = it) },
                label = { Text("Устройство создания") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = exifData.model,
                onValueChange = { exifData = exifData.copy(model = it) },
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
                        val success = if (saveAsCopy) {
                            val newUri = ExifUtils.saveExifDataAsCopy(
                                context,
                                Uri.parse(imageUri),
                                exifData
                            )
                            newUri != null
                        } else {
                            ExifUtils.saveExifData(
                                context,
                                Uri.parse(imageUri),
                                exifData
                            )
                        }
                        saveSuccess = success
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}