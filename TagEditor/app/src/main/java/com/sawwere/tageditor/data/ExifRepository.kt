package com.sawwere.tageditor.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ExifRepository {
    suspend fun readExifData(uri: Uri): ExifData
    suspend fun saveExifData(originalUri: Uri, exifData: ExifData): Boolean
}

class ExifRepositoryImpl(private val context: Context) : ExifRepository {

    override suspend fun readExifData(uri: Uri): ExifData {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    val exif = ExifInterface(pfd.fileDescriptor)

                    val latitude = parseExifGpsCoordinate(
                        exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                        exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                    )

                    val longitude = parseExifGpsCoordinate(
                        exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                        exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
                    )

                    ExifData(
                        dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "",
                        latitude = latitude,
                        longitude = longitude,
                        make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: "",
                        model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                    )
                } ?: ExifData()
            } catch (e: Exception) {
                e.printStackTrace()
                ExifData()
            }
        }
    }

    override suspend fun saveExifData(originalUri: Uri, exifData: ExifData): Boolean {
        return withContext(Dispatchers.IO) {
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                parcelFileDescriptor = context.contentResolver.openFileDescriptor(originalUri, "rw")
                parcelFileDescriptor?.let { pfd ->
                    val exif = ExifInterface(pfd.fileDescriptor)

                    if (exifData.dateTime.isNotEmpty()) {
                        exif.setAttribute(ExifInterface.TAG_DATETIME, exifData.dateTime)
                    } else {
                        exif.setAttribute(ExifInterface.TAG_DATETIME, null)
                    }

                    if (exifData.latitude != null && exifData.longitude != null) {
                        val (latValue, latRef) = convertToExifGpsFormat(exifData.latitude, true)
                        val (lonValue, lonRef) = convertToExifGpsFormat(exifData.longitude, false)

                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latValue)
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latRef)
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lonValue)
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, lonRef)
                    } else {
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
                        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
                        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
                    }
                    if (exifData.make.isNotEmpty()) {
                        exif.setAttribute(ExifInterface.TAG_MAKE, exifData.make)
                    } else {
                        exif.setAttribute(ExifInterface.TAG_MAKE, null)
                    }

                    if (exifData.model.isNotEmpty()) {
                        exif.setAttribute(ExifInterface.TAG_MODEL, exifData.model)
                    } else {
                        exif.setAttribute(ExifInterface.TAG_MODEL, null)
                    }

                    exif.saveAttributes()
                    true
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                parcelFileDescriptor?.close()
            }
        }
    }

    /**
     * Преобразует десятичные координаты в EXIF GPS формат
     */
    private fun convertToExifGpsFormat(coordinate: Double, isLatitude: Boolean): Pair<String, String> {
        val ref = when {
            isLatitude && coordinate >= 0 -> "N"
            isLatitude && coordinate < 0 -> "S"
            !isLatitude && coordinate >= 0 -> "E"
            else -> "W"
        }

        val absCoordinate = Math.abs(coordinate)
        val degrees = absCoordinate.toInt()
        val minutes = ((absCoordinate - degrees) * 60).toInt()
        val seconds = ((absCoordinate - degrees - minutes / 60.0) * 3600)

        // EXIF формат: "degrees/1,minutes/1,seconds/100"
        return Pair("$degrees/1,$minutes/1,${(seconds * 100).toInt()}/100", ref)
    }

    /**
     * Парсит EXIF GPS координату в десятичное число
     */
    private fun parseExifGpsCoordinate(coordinate: String?, ref: String?): Double? {
        if (coordinate == null) return null

        try {
            val parts = coordinate.split(",")
            if (parts.size != 3) return null

            val degrees = parseRational(parts[0])
            val minutes = parseRational(parts[1])
            val seconds = parseRational(parts[2])

            if (degrees == null || minutes == null || seconds == null) return null

            var result = degrees + minutes / 60.0 + seconds / 3600.0

            when (ref) {
                "S", "W" -> result = -result
            }

            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Парсит рациональное число в формате "числитель/знаменатель"
     */
    private fun parseRational(rational: String): Double? {
        return try {
            val parts = rational.split("/")
            if (parts.size != 2) return null
            val numerator = parts[0].toDouble()
            val denominator = parts[1].toDouble()
            if (denominator == 0.0) null else numerator / denominator
        } catch (e: Exception) {
            null
        }
    }
}