package com.sawwere.tageditor.ui.edit

import com.sawwere.tageditor.data.ExifData

data class EditState(
    val imageUri: String = "",
    val exifData: ExifData = ExifData(),
    val saveAsCopy: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean? = null,
    val error: String? = null
)