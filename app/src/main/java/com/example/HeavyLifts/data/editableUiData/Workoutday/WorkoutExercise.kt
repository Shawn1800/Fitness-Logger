package com.example.demo103.data.editableUiData.Workoutday

data class EditableWorkoutExercise(
    val workoutExerciseId: Int,
    val exerciseName: String,
    val sets: List<EditableSet>,
    val isExpanded: Boolean = false
)

data class EditableSet(
    val id: Int? = null, // null = not saved yet
    val weight: String = "",
    val reps: String = ""
)