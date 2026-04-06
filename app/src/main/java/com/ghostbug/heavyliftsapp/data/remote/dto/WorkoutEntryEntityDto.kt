package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime

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
    val millis = try {
        Instant.parse(date).toEpochMilli()
    } catch (e: Exception) {
        try {
            OffsetDateTime.parse(date).toInstant().toEpochMilli()
        } catch (e2: Exception) {
            // Fallback or handle other formats if necessary
            0L
        }
    }
    return WorkoutEntryEntity(
        id = id ?: 0,
        exerciseId = exerciseId,
        weight = weight,
        reps = reps,
        sets = sets,
        date = millis,
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
