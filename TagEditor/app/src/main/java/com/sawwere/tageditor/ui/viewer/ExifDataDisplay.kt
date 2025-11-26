package com.sawwere.tageditor.ui.viewer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sawwere.tageditor.data.ExifData

@Composable
fun ExifDataDisplay(
    exifData: ExifData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "EXIF данные",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ExifDataItem(
                label = "Дата создания",
                value = exifData?.dateTime,
                fallback = "нет данных"
            )

            ExifDataItem(
                label = "Широта",
                value = exifData?.latitude?.toString(),
                fallback = "нет данных"
            )

            ExifDataItem(
                label = "Долгота",
                value = exifData?.longitude?.toString(),
                fallback = "нет данных"
            )

            ExifDataItem(
                label = "Устройство",
                value = exifData?.make,
                fallback = "нет данных"
            )

            ExifDataItem(
                label = "Модель",
                value = exifData?.model,
                fallback = "нет данных"
            )
        }
    }
}

@Composable
private fun ExifDataItem(
    label: String,
    value: String?,
    fallback: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value?.takeIf { it.isNotEmpty() } ?: fallback,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value?.isNotEmpty() == true) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}