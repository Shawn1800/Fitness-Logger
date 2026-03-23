package com.example.demo103.ui.theme.home

import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.entity.WorkoutWithExercise
import java.time.LocalDate

data class HomeState (
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateMillis: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val workouts: List<GroupedWorkout> = emptyList(),
    )