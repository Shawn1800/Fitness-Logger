package com.example.HeavyLifts.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WorkoutWithExercise(
    @Embedded val workoutEntry: WorkoutEntryEntity,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "exercise_id"
    )
    val exercise: ExerciseEntity
)
