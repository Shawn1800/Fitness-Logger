package com.ghostbug.heavyliftsapp.screens.home

import java.time.LocalDate

data class HomeState (
    val selectedDate: LocalDate = LocalDate.now(),
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