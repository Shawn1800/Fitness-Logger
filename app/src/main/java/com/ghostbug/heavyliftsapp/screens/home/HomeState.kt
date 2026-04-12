package com.ghostbug.heavyliftsapp.screens.home

import java.time.LocalDate

data class HomeState (
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateMillis: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val workouts: List<GroupedWorkout> = emptyList(),
    )