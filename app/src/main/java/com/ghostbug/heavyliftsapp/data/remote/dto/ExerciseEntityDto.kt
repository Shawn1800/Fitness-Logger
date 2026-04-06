package com.ghostbug.heavyliftsapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ExerciseEntityDto(
    @SerialName("id") val id: Int,
    @SerialName("exercise_name") val exerciseName: String,
    @SerialName("category") val category: String
)
