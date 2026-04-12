package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.SerialName

data class ExerciseEntity(
    val id: Long,
    @SerialName("exercise_name")
    val exerciseName: String,
    val category: String,

)