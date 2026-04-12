package com.ghostbug.heavyliftsapp.screens.home

sealed interface HomeUiEvent {
    data class NavigateToLogWorkout(
        val exerciseId: Long,
        val dateMillis: Long
    ) : HomeUiEvent

    data class NavigateToExerciseSelection(
        val dateMillis: Long
    ) : HomeUiEvent

    data class ShowSnackbar(val message: String) : HomeUiEvent
}