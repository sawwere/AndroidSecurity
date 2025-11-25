package com.sawwere.tageditor.ui.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sawwere.tageditor.data.ExifData
import com.sawwere.tageditor.data.ExifUtils
import com.sawwere.tageditor.ui.navigation.Screen
import com.sawwere.tageditor.ui.viewer.ExifDataDisplay
import com.sawwere.tageditor.ui.viewer.ImageViewer

@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var exifData by remember { mutableStateOf<ExifData?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it.toString()
            exifData = ExifUtils.readExifData(context, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "EXIF Editor",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Выбрать изображение")
        }

        selectedImageUri?.let { uri ->
            ImageViewer(
                imageUri = uri,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp)
            )

            ExifDataDisplay(
                exifData = exifData,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    navController.navigate(Screen.Edit.createRoute(uri))
                }
            ) {
                Text("Редактировать EXIF теги")
            }
        }
    }
}