package com.example.inventory.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class AppSettingsManager(context: Context) {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "encrypted_app_preferences",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var hideSensitiveData: Boolean
        get() = sharedPreferences.getBoolean(KEY_HIDE_SENSITIVE_DATA, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_HIDE_SENSITIVE_DATA, value).apply()

    var disableSharing: Boolean
        get() = sharedPreferences.getBoolean(KEY_DISABLE_SHARING, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_DISABLE_SHARING, value).apply()

    var useDefaultQuantity: Boolean
        get() = sharedPreferences.getBoolean(KEY_USE_DEFAULT_QUANTITY, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_USE_DEFAULT_QUANTITY, value).apply()

    var defaultQuantity: String
        get() = sharedPreferences.getString(KEY_DEFAULT_QUANTITY, "1") ?: "1"
        set(value) = sharedPreferences.edit().putString(KEY_DEFAULT_QUANTITY, value).apply()

    companion object {
        private const val KEY_HIDE_SENSITIVE_DATA = "hide_sensitive_data"
        private const val KEY_DISABLE_SHARING = "disable_sharing"
        private const val KEY_USE_DEFAULT_QUANTITY = "use_default_quantity"
        private const val KEY_DEFAULT_QUANTITY = "default_quantity"
    }
}