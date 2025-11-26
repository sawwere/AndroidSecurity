package com.sawwere.tageditor.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface

interface ExifRepository {
    suspend fun readExifData(uri: Uri): ExifData
    suspend fun saveExifDataAsCopy(originalUri: Uri, exifData: ExifData): Uri?
}

class ExifRepositoryImpl(private val context: Context) : ExifRepository {

    override suspend fun readExifData(uri: Uri): ExifData {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                val latitudeStr = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
                val longitudeStr = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)

                val latitude = latitudeStr?.toDoubleOrNull()
                val longitude = longitudeStr?.toDoubleOrNull()

                ExifData(
                    dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "",
                    latitude = if (latitude == 0.0) null else latitude,
                    longitude = if (longitude == 0.0) null else longitude,
                    make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "",
                    model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                )
            } ?: ExifData()
        } catch (e: Exception) {
            e.printStackTrace()
            ExifData()
        }
    }

    override suspend fun saveExifDataAsCopy(originalUri: Uri, exifData: ExifData): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "edited_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EXIFEditor")
                }
            }

            val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val newUri = context.contentResolver.insert(collection, contentValues) ?: return null

            copyImageWithExif(originalUri, newUri, exifData)
            newUri

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun copyImageWithExif(sourceUri: Uri, destUri: Uri, exifData: ExifData) {
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                context.contentResolver.openOutputStream(destUri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            updateExifInUri(destUri, exifData)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun updateExifInUri(uri: Uri, exifData: ExifData) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return
            val tempData = inputStream.use { it.readBytes() }
            inputStream.close()

            val tempFile = java.io.File.createTempFile("exif_temp", ".jpg")
            tempFile.writeBytes(tempData)

            val exif = ExifInterface(tempFile)
            updateExifAttributes(exif, exifData)
            exif.saveAttributes()

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                tempFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            tempFile.delete()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateExifAttributes(exif: ExifInterface, exifData: ExifData) {
        if (exifData.dateTime.isNotEmpty()) {
            exif.setAttribute(ExifInterface.TAG_DATETIME, exifData.dateTime)
        }

        if (exifData.latitude != null && exifData.longitude != null) {
            exif.setLatLong(exifData.latitude, exifData.longitude)
        }

        if (exifData.make.isNotEmpty()) {
            exif.setAttribute(ExifInterface.TAG_MAKE, exifData.make)
        }

        if (exifData.model.isNotEmpty()) {
            exif.setAttribute(ExifInterface.TAG_MODEL, exifData.model)
        }
    }
}