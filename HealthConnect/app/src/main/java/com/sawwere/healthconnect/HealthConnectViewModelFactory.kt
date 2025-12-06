package com.sawwere.healthconnect

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sawwere.healthconnect.data.HealthConnectProvider
import com.sawwere.healthconnect.ui.HealthConnectViewModel

class HealthConnectViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthConnectViewModel::class.java)) {
            val provider = HealthConnectProvider(context)
            return HealthConnectViewModel(provider) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}