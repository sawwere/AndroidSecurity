package com.example.inventory.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EncryptedFileManager(private val context: Context) {

    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val gson = Gson()

    private fun getEncryptedFile(tempFile: File): EncryptedFile {
        return EncryptedFile.Builder(
            tempFile,
            context,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    suspend fun saveItemToFile(item: Item, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "temp_item_file")
        try {
            val encryptedFile = getEncryptedFile(tempFile)

            encryptedFile.openFileOutput().use { outputStream ->
                outputStream.write(gson.toJson(item.copy(id = 0)).toByteArray(StandardCharsets.UTF_8))
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(tempFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("EncryptedFileManager", "Error saving encrypted file", e)
            false
        } finally {
            tempFile.delete()
        }
    }

    suspend fun loadItemFromFile(uri: Uri): Item? = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "temp_item_file")
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val encryptedFile = getEncryptedFile(tempFile)
            encryptedFile.openFileInput().use { inputStream ->
                val jsonString = inputStream.readBytes().toString(StandardCharsets.UTF_8)
                return@withContext gson.fromJson(jsonString, Item::class.java)
            }
        } catch (e: Exception) {
            Log.e("EncryptedFileManager", "Error loading encrypted file", e)
            null
        } finally {
            tempFile.delete()
        }
    }

    fun generateFileName(item: Item): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeName = item.name.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return "item_${safeName}_$timestamp.enc"
    }
}