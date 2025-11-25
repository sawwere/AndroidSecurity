package com.sawwere.tageditor.ui.viewer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sawwere.tageditor.data.ExifData

@Composable
fun ExifDataDisplay(exifData: ExifData?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "EXIF данные:",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Дата создания: ${exifData?.dateTime ?: "Не указана"}")
            Text("Широта: ${exifData?.latitude ?: "Не указана"}")
            Text("Долгота: ${exifData?.longitude ?: "Не указана"}")
            Text("Устройство: ${exifData?.make ?: "Не указано"}")
            Text("Модель: ${exifData?.model ?: "Не указана"}")
        }
    }
}