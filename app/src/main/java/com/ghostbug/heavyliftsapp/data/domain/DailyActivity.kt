package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.datetime.LocalDate
import kotlin.time.Clock.System.now
import kotlin.time.Instant


data class DailyActivity (
    val id: String?=null,
    val userId: String,
    val date: LocalDate,
    val steps: Int=0,
    val stepSource:String="sensor",
    val caloriesBurned: Float=0f,
    val calorieSource:String="estimated",
    val distanceKm: Float=0f,
    val createdAt:Instant,
    val updatedAt:Instant,
    val activeMinutes:Int=0
    )
