package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import java.time.OffsetDateTime
import kotlin.time.Instant

@Serializable
data class WorkoutEntryEntityDto(
    @SerialName("id") val id: Long?=null,
    @SerialName("exercise_id") val exerciseId: Long,
    @SerialName("weight") val weight: Float,
    @SerialName("reps") val reps: Int,
    @SerialName("sets") val sets: Int,
    @SerialName("date") val date: Instant,
    @SerialName("user_id") val userId: String
)

fun WorkoutEntryEntityDto.toDomain(): WorkoutEntryEntity {

    return WorkoutEntryEntity(
        id = id ?: 0L,
        exerciseId = exerciseId,
        weight = weight,
        reps = reps,
        sets = sets,
        date = date.toEpochMilliseconds(),
        userId = userId
    )
}

fun WorkoutEntryEntity.toDto(): WorkoutEntryEntityDto {
    return WorkoutEntryEntityDto(
        id = if (id == 0L) null  else id,
        exerciseId = exerciseId,
        weight = weight,
        reps = reps,
        sets = sets,
        date = Instant.fromEpochMilliseconds(date),
        userId = userId
    )
}
