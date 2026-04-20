package com.ghostbug.heavyliftsapp.screens.log_workout

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity

data class LogWorkoutState(
    val selectedExerciseId: Long? = null,
    val sets: List<WorkoutEntryEntity> = emptyList(),
    val exercise: ExerciseEntity? = null,
    val dateMillis: Long = 0L,
    val isReadOnly: Boolean = false
)
