package com.sawwere.healthconnect.ui

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePick(
    time: MutableState<Long>,
    modifier: Modifier = Modifier
) {
    val dateState = rememberDatePickerState()
    var dateDialogController by remember { mutableStateOf(false) }

    LaunchedEffect(dateState.selectedDateMillis) {
        dateState.selectedDateMillis?.let {
            time.value = it / 1000
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = SimpleDateFormat("MM/dd/yyyy").format(Date(time.value * 1000)),
            fontSize = 18.sp
        )
        Button(
            onClick = { dateDialogController = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff5d00ff)
            ),
            modifier = Modifier.height(40.dp)
        ) {
            Text(text = "Change Date", fontSize = 14.sp)
        }
    }

    if (dateDialogController) {
        DatePickerDialog(
            onDismissRequest = { dateDialogController = false },
            confirmButton = {
                TextButton(onClick = { dateDialogController = false }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { dateDialogController = false }) {
                    Text(text = "Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }
}

fun LocalDate.toEpochSeconds(): Long {
    return this.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .epochSecond
}

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochSecond(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}