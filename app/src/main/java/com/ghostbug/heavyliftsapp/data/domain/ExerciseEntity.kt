package com.ghostbug.heavyliftsapp.data.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseEntity(
    val id: Int,
    @SerialName("exercise_name") val exerciseName: String,
    val category: String,

)