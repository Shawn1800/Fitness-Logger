package com.ghostbug.heavyliftsapp.data.domain


import kotlinx.datetime.LocalDate
import kotlin.time.Clock.System.now
import kotlin.time.Instant

data class  ActivityGoals (
    val id:String?=null,
    val userId: String,
    val stepGoal: Int=10000,
    val calorieGoal: Float=500f,
    val effectiveFrom: LocalDate,
    val createdAt: Instant
)
