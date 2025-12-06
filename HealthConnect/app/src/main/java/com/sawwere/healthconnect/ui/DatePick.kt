package com.sawwere.healthconnect.ui

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePick(
    time: MutableState<Long>,
    modifier: Modifier = Modifier
) {
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = if (time.value > 0) {
            time.value * 1000
        } else {
            null
        }
    )
    var dateDialogController by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    }

    val formattedDate = remember(time.value) {
        if (time.value > 0) {
            try {
                dateFormatter.format(Date(time.value * 1000))
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    LaunchedEffect(dateState.selectedDateMillis) {
        dateState.selectedDateMillis?.let {
            time.value = it / 1000
        }
    }

    OutlinedTextField(
        value = formattedDate,
        onValueChange = {},
        readOnly = true,
        label = { Text("Select date") },
        modifier = modifier
            .fillMaxWidth()
            .noRippleClickable {
                dateDialogController = true
            },
        trailingIcon = {
            IconButton(
                onClick = {
                    dateDialogController = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date"
                )
            }
        }
    )

    if (dateDialogController) {
        DatePickerDialog(
            onDismissRequest = {
                println("DEBUG: Dialog dismissed")
                dateDialogController = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        println("DEBUG: OK clicked")
                        dateDialogController = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("OK", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        println("DEBUG: Cancel clicked")
                        dateDialogController = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        ) {
            DatePicker(
                state = dateState,
                title = {
                    if (dateState.displayMode == DisplayMode.Picker) {
                        Text(
                            "Select date",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            )
        }
    }
}

private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed(
    factory = {
        this.then(
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        )
    }
)

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