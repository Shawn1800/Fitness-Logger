package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.Serializable


data class WorkoutWithExercise(
    val id: Long,
    val weight: Float,
    val reps: Int,
    val sets: Int,
    val date: Long,
    val userId: String,
    val exercise: ExerciseEntity
)
