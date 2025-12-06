package com.sawwere.healthconnect.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateRangeFilter(
    startDate: LocalDate,
    endDate: LocalDate,
    onDateRangeChanged: (LocalDate, LocalDate) -> Unit
) {
    val startTimestamp = remember { mutableStateOf(startDate.toEpochSeconds()) }
    val endTimestamp = remember { mutableStateOf(endDate.toEpochSeconds()) }

    LaunchedEffect(startTimestamp.value) {
        val newStartDate = startTimestamp.value.toLocalDate()
                val currentEndDate = endTimestamp.value.toLocalDate()
        if (!newStartDate.isAfter(currentEndDate)) {
            onDateRangeChanged(newStartDate, currentEndDate)
        } else {
            endTimestamp.value = newStartDate.toEpochSeconds()
            onDateRangeChanged(newStartDate, newStartDate)
        }
    }

    LaunchedEffect(endTimestamp.value) {
        val newEndDate = endTimestamp.value.toLocalDate()
        val currentStartDate = startTimestamp.value.toLocalDate()
        if (!newEndDate.isBefore(currentStartDate)) {
            onDateRangeChanged(currentStartDate, newEndDate)
        } else {
            startTimestamp.value = newEndDate.toEpochSeconds()
            onDateRangeChanged(newEndDate, newEndDate)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Date Range:",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Date", style = MaterialTheme.typography.labelSmall)
                DatePick(
                    time = startTimestamp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Text("to", modifier = Modifier.padding(horizontal = 8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text("End Date", style = MaterialTheme.typography.labelSmall)
                DatePick(
                    time = endTimestamp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}