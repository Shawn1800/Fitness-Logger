package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.ActivityGoals
import com.ghostbug.heavyliftsapp.data.domain.DailyActivity
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ActivityGoalsDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("step_goal") val stepGoal: Int = 10000,       // add SerialName
    @SerialName("calorie_goal") val calorieGoal: Float = 500f, // add SerialName
    @SerialName("effective_from") val effectiveFrom: LocalDate, // not nullable, always required
    @SerialName("created_at") val createdAt: Instant? = null
)

fun ActivityGoalsDto.toDomain(): ActivityGoals {
    return ActivityGoals(
        id = id,
        userId = userId,
        stepGoal = stepGoal,
        calorieGoal = calorieGoal,
        effectiveFrom = effectiveFrom,
        createdAt = createdAt
    )
}

fun ActivityGoals.toDto(): ActivityGoalsDto {
    return ActivityGoalsDto(
        id = id,
        userId = userId,
        stepGoal = stepGoal,
        calorieGoal = calorieGoal,
        effectiveFrom = effectiveFrom,
        createdAt = createdAt
    )
}