package com.ghostbug.heavyLifts.data.remote.dto

import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class WorkoutEntryEntityDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("exercise_id") val exerciseId: Int,
    @SerialName("weight") val weight: Float,
    @SerialName("reps") val reps: Int,
    @SerialName("sets") val sets: Int,
    @SerialName("date") val date: String,
    @SerialName("user_id") val userId: String
)

fun WorkoutEntryEntityDto.toDomain(): WorkoutEntryEntity {
    return WorkoutEntryEntity(
        id = id ?: 0,
        exerciseId = exerciseId,
        weight = weight,
        reps = reps,
        sets = sets,
        date = Instant.parse(date).toEpochMilli(),
        userId = userId
    )
}

fun WorkoutEntryEntity.toDto(): WorkoutEntryEntityDto {
    return WorkoutEntryEntityDto(
        id = if (id == 0) null else id,
        exerciseId = exerciseId,
        weight = weight,
        reps = reps,
        sets = sets,
        date = Instant.ofEpochMilli(date).toString(),
        userId = userId
    )
}
