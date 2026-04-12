package com.ghostbug.heavyliftsapp.screens.home

import java.time.LocalDate

sealed interface HomeEvent {
    data class OnDateSelected(val date: LocalDate) : HomeEvent
    object OnAddWorkoutClick : HomeEvent

    data class EditWorkout(val exerciseId: Long) : HomeEvent

    object RefreshWorkouts : HomeEvent

    data class message(val message: String): HomeEvent

    //    data class DeleteExercise(val exercise: ExerciseEntity): ExerciseUiEvent
}