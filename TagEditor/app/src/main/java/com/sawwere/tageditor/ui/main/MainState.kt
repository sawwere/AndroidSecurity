package com.sawwere.tageditor.ui.main

import com.sawwere.tageditor.data.ExifData

data class MainState(
    val selectedImageUri: String? = null,
    val exifData: ExifData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)