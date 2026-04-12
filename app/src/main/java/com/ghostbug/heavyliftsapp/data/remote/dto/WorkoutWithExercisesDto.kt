package com.ghostbug.heavyliftsapp.data.remote.dto

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutWithExercise
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import kotlin.time.Instant


@Serializable
data class WorkoutWithExerciseDto(
    @SerialName("id") val id: Long,
    @SerialName("weight") val weight: Float,
    @SerialName("reps") val reps: Int,
    @SerialName("sets") val sets: Int,
    @SerialName("date") val date: Instant,
    @SerialName("user_id") val userId: String,
    @SerialName("exercises") val exercise: ExerciseEntityDto
)

fun WorkoutWithExerciseDto.toDomain(): WorkoutWithExercise {

    return WorkoutWithExercise(
        id = id,
        weight = weight,
        reps = reps,
        sets = sets,
        date = date.toEpochMilliseconds(),
        exercise = ExerciseEntity(
            id = exercise.id,
            exerciseName = exercise.exerciseName,
            category = exercise.category,

        ),
        userId = userId
    )
}