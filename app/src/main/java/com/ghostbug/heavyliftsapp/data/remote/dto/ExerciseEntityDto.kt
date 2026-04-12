package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ExerciseEntityDto(
    @SerialName("id") val id: Long,
    @SerialName("exercise_name") val exerciseName: String,
    @SerialName("category") val category: String
)

fun ExerciseEntityDto.toDomain(): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        exerciseName = exerciseName,
        category = category
    )
}

 fun ExerciseEntity.toDto(): ExerciseEntityDto {
    return ExerciseEntityDto(
        id = id,
        exerciseName = exerciseName,
        category = category
    )
}
