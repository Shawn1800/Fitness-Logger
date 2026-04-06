package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutWithExercise(
    val id: Int,
    val weight: Double,
    val reps: Int,
    val sets: Int,
    val date: Long,
    val userId: String,
    val exercise: ExerciseEntity
)
