package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.DailyActivity
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DailyActivityDto (
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("date") val date: LocalDate,          // not nullable, always required
    @SerialName("steps") val steps: Int = 0,          // remove nullable, default is fine
    @SerialName("step_source") val stepSource: String = "sensor",
    @SerialName("calories_burned") val caloriesBurned: Float = 0f,
    @SerialName("calorie_source") val calorieSource: String = "estimated", // fix: was "calories_source"
    @SerialName("distance_km") val distanceKm: Float = 0f,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("updated_at") val updatedAt: Instant? = null,
    @SerialName("active_minutes")val activeMinutes:Int?=0
)


fun DailyActivityDto.toDomain(): DailyActivity{
    return DailyActivity(
        id = id,
        userId = userId,
        date = date,
        steps = steps,
        stepSource = stepSource,
        caloriesBurned = caloriesBurned,
        calorieSource = calorieSource,
        distanceKm = distanceKm,
        createdAt = createdAt,
        updatedAt = updatedAt,
        activeMinutes = activeMinutes
    )
}

fun DailyActivity.toDto(): DailyActivityDto{
    return DailyActivityDto(
        id = id,
        userId = userId,
        date = date ,
        steps = steps,
        stepSource = stepSource,
        caloriesBurned = caloriesBurned,
        calorieSource = calorieSource,
        distanceKm = distanceKm,
        createdAt = createdAt,
        updatedAt = updatedAt,
        activeMinutes = activeMinutes
    )
}