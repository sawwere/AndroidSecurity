package com.sawwere.healthconnect.data

import java.time.Instant
import java.time.LocalDate

sealed class HealthData {
    abstract val id: String
    abstract val date: LocalDate

    data class StepData(
        override val id: String,
        val count: Long,
        override val date: LocalDate,
        val startTime: Instant,
        val endTime: Instant
    ) : HealthData()

    data class WeightData(
        override val id: String,
        val weight: Double,
        override val date: LocalDate,
        val time: Instant
    ) : HealthData()
}

enum class DataType {
    STEPS, WEIGHT
}