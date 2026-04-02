package com.ghostbug.heavyLifts.ui.screen.home

import com.ghostbug.heavyLifts.data.domain.ExerciseEntity
import java.time.LocalDate

sealed interface HomeEvent {
    data class OnDateSelected(val date: LocalDate) : HomeEvent // data class is used when the event carries info
    object OnAddWorkoutClick : HomeEvent  // fab  // here object is used when the event carries no data

    data class EditWorkout(val exercise : ExerciseEntity) : HomeEvent

    object RefreshWorkouts : HomeEvent // Refresh workouts for current date

    //    data class DeleteExercise(val exercise: ExerciseEntity): ExerciseUiEvent
}