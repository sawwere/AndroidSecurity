package com.sawwere.tageditor.data

import android.content.Context

interface AppContainer {
    val exifRepository: ExifRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Implementation for [ExifRepository]
     */
    override val exifRepository: ExifRepository by lazy {
        ExifRepositoryImpl(context)
    }
}