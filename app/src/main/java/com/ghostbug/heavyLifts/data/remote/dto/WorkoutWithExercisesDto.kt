package com.ghostbug.heavyLifts.data.remote.dto

import com.ghostbug.heavyLifts.data.domain.ExerciseEntity
import com.ghostbug.heavyLifts.data.domain.WorkoutWithExercise
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime


@Serializable
data class WorkoutWithExerciseDto(
    @SerialName("id") val id: Int,
    @SerialName("weight") val weight: Double,
    @SerialName("reps") val reps: Int,
    @SerialName("sets") val sets: Int,
    @SerialName("date") val date: String,
    @SerialName("exercises") val exercise: ExerciseEntityDto
)

fun WorkoutWithExerciseDto.toDomain(): WorkoutWithExercise {
    val millis = try {
        Instant.parse(date).toEpochMilli()
    } catch (e: Exception) {
        try {
            OffsetDateTime.parse(date).toInstant().toEpochMilli()
        } catch (e2: Exception) {
            0L
        }
    }
    return WorkoutWithExercise(
        id = id,
        weight = weight,
        reps = reps,
        sets = sets,
        date = millis,
        exercise = ExerciseEntity(
            id = exercise.id,
            exerciseName = exercise.exerciseName,
            category = exercise.category,
//            userId = exercise.userId
        ),
        userId = ""
    )
}