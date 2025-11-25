package com.sawwere.tageditor.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sawwere.tageditor.TagEditorApplication
import com.sawwere.tageditor.ui.edit.EditViewModel
import com.sawwere.tageditor.ui.main.MainViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for MainViewModel
        initializer {
            MainViewModel(
                exifRepository = exifEditorApplication().container.exifRepository
            )
        }
        // Initializer for EditViewModel
        initializer {
            EditViewModel(
                exifRepository = exifEditorApplication().container.exifRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [ExifEditorApplication].
 */
fun CreationExtras.exifEditorApplication(): TagEditorApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TagEditorApplication)