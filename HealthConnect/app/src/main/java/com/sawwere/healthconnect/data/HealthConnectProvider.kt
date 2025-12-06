package com.sawwere.healthconnect.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord

class HealthConnectProvider(context: Context) {
    var permissionsGranted = false

    companion object {
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getWritePermission(WeightRecord::class)
        )
    }

    private var _client: HealthConnectClient? = null

    val client: HealthConnectClient
        get() = _client ?: throw IllegalStateException("HealthConnectClient not initialized")

    private lateinit var _repository: HealthConnectRepository

    val repository: HealthConnectRepository
        get() = _repository

    init {
        createClient(context)
    }

    private fun createClient(context: Context) {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            _client = HealthConnectClient.getOrCreate(context)
            _repository = HealthConnectRepository(_client!!)
        } else {
            _client = null
            _repository = HealthConnectRepository(null)
        }
    }

    suspend fun checkPermissions(): Boolean {
        return try {
            val granted = _client?.permissionController?.getGrantedPermissions()
            permissionsGranted = granted?.containsAll(PERMISSIONS) ?: false
            permissionsGranted
        } catch (e: Exception) {
            false
        }
    }
}