package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlin.time.Clock.System.now
import kotlin.time.Instant


data class DailyActivity(
    val id: String? = null,
    val userId: String,
    val date: LocalDate,          // not nullable, always required
    val steps: Int = 0,          // remove nullable, default is fine
    val stepSource: String = "sensor",
    val caloriesBurned: Float = 0f,
    val calorieSource: String = "estimated", // fix: was "calories_source"
    val distanceKm: Float = 0f,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val activeMinutes: Int? = 0
)
