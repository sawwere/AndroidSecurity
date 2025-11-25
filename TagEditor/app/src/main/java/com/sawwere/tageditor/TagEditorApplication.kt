package com.sawwere.tageditor

import android.app.Application
import com.sawwere.tageditor.data.AppContainer
import com.sawwere.tageditor.data.AppDataContainer

class TagEditorApplication: Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}