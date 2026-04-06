package com.ghostbug.heavyliftsapp.ui.screen.home

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity



sealed interface HomeUiEvent {
    data class NavigateToLogWorkout(
        val exercise: ExerciseEntity,
        val dateMillis: Long
    ) : HomeUiEvent

    data class NavigateToExerciseSelection(
        val dateMillis: Long
    ) : HomeUiEvent

    data class ShowSnackbar(val message: String) : HomeUiEvent
}