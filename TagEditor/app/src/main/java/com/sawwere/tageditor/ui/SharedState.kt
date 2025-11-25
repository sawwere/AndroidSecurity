package com.sawwere.tageditor.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SharedState {
    var selectedImageUri: String? by mutableStateOf(null)
}