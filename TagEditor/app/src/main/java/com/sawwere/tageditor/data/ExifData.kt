package com.sawwere.tageditor.data

data class ExifData(
    val dateTime: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val make: String = "",
    val model: String = ""
)