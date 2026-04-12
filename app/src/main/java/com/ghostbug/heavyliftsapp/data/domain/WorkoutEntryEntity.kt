package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.Serializable

data class WorkoutEntryEntity(
    val id: Long=0L,
    val exerciseId: Long,
    val weight: Float,
    val reps: Int,
    val sets: Int,
    val date: Long,
    val userId: String
)
