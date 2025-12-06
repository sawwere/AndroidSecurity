package com.sawwere.healthconnect.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.getOrCreate
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.permission.HealthPermission.Companion.getReadPermission
import androidx.health.connect.client.permission.HealthPermission.Companion.getWritePermission
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId



class HealthConnectRepository(private val client: HealthConnectClient?) {

    suspend fun insertSteps(
        count: Long,
        date: LocalDate
    ): Boolean {
        if (client == null) return false

        return try {
            val startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val stepsRecord = StepsRecord(
                count = count,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = null,
                endZoneOffset = null
            )

            client.insertRecords(listOf(stepsRecord))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun insertWeight(
        weightKg: Double,
        date: LocalDate
    ): Boolean {
        if (client == null) return false

        return try {
            val time = date.atTime(12, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()

            val weightRecord = WeightRecord(
                weight = Mass.kilograms(weightKg),
                time = time,
                zoneOffset = null
            )

            client.insertRecords(listOf(weightRecord))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun readStepsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<StepsRecord> {
        if (client == null) return emptyList()

        return try {
            val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            client.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            ).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun readWeightByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WeightRecord> {
        if (client == null) return emptyList()

        return try {
            val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            client.readRecords(
                ReadRecordsRequest(
                    WeightRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            ).records
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteStepsById(recordId: String): Boolean {
        if (client == null) return false

        return try {
            client.deleteRecords(
                StepsRecord::class,
                recordIdsList = listOf(recordId),
                clientRecordIdsList = emptyList()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteWeightById(recordId: String): Boolean {
        if (client == null) return false

        return try {
            client.deleteRecords(
                WeightRecord::class,
                recordIdsList = listOf(recordId),
                clientRecordIdsList = emptyList()
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun aggregateStepsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Long {
        if (client == null) return 0L

        return try {
            val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

