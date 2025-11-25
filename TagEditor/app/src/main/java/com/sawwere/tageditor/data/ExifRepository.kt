package com.sawwere.tageditor.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

interface ExifRepository {
    suspend fun readExifData(uri: Uri): ExifData
    suspend fun saveExifData(uri: Uri, exifData: ExifData): Boolean
    suspend fun saveExifDataAsCopy(originalUri: Uri, exifData: ExifData): Uri?
}

class ExifRepositoryImpl(private val context: Context) : ExifRepository {

    override suspend fun readExifData(uri: Uri): ExifData {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                ExifData(
                    dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "",
                    latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.toDouble(),
                    longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)?.toDouble(),
                    make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "",
                    model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                )
            } ?: ExifData()
        } catch (e: Exception) {
            e.printStackTrace()
            ExifData()
        }
    }

    override suspend fun saveExifData(uri: Uri, exifData: ExifData): Boolean {
        return try {
            val tempFile = File.createTempFile("exif_edit", ".jpg", context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val exif = ExifInterface(tempFile)
            updateExifAttributes(exif, exifData)
            exif.saveAttributes()

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                tempFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            tempFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun saveExifDataAsCopy(originalUri: Uri, exifData: ExifData): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "edited_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EXIFEditor")
            }

            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val newUri = context.contentResolver.insert(collection, contentValues)

            newUri?.let { uri ->
                val tempFile = File.createTempFile("exif_edit", ".jpg", context.cacheDir)

                context.contentResolver.openInputStream(originalUri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }

                    val exif = ExifInterface(tempFile)
                    updateExifAttributes(exif, exifData)
                    exif.saveAttributes()

                    context.contentResolver.openOutputStream(uri)?.use { finalOutputStream ->
                        tempFile.inputStream().use { tempInput ->
                            tempInput.copyTo(finalOutputStream)
                        }
                    }
                }

                tempFile.delete()
                uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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