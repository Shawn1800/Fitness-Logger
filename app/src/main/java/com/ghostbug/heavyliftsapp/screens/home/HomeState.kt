package com.ghostbug.heavyliftsapp.screens.home

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class HomeState(
    val selectedDate: LocalDate =Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val selectedDateMillis: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val workouts: List<GroupedWorkout> = emptyList(),
    val todaySteps: Int = 0,
    val stepGoal: Int = 10000,
    val stepProgress: Float = 0f,
    val stepSource: String = "sensor",
    val todayCalories: Float = 0f,
    val todayDistanceKm: Float = 0f
)
