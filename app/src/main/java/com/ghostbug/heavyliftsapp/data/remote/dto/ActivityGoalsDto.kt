package com.ghostbug.heavyliftsapp.data.remote.dto

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ActivityGoalsDto(
    val id:String?=null,
    val userId: String,
    val stepGoal: Int=10000,
    val calorieGoal: Float=500f,
    val effectiveFrom: LocalDate,
    val createdAt: Instant
)
