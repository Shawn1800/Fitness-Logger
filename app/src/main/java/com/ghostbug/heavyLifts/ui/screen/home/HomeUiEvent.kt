package com.ghostbug.heavyLifts.ui.screen.home

import com.ghostbug.heavyLifts.data.domain.ExerciseEntity



sealed interface HomeUiEvent {
    data class NavigateToLogWorkout(
        val exercise: ExerciseEntity,
        val dateMillis: Long
    ) : HomeUiEvent

    data class NavigateToExerciseSelection(
        val dateMillis: Long
    ) : HomeUiEvent

}