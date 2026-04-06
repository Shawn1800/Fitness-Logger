package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.Serializable
@Serializable
data class WorkoutEntryEntity(
    val id: Int,
    val exerciseId: Int,
    val weight: Float,
    val reps: Int,
    val sets: Int,
    val date: Long,
    val userId: String
)
