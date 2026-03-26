package com.example.HeavyLifts.ui.screen.home

import com.example.HeavyLifts.data.entity.ExerciseEntity
import java.time.LocalDate

sealed interface HomeEvent {
    data class OnDateSelected(val date: LocalDate) : HomeEvent // data class is used when the event carries info
    object OnAddWorkoutClick : HomeEvent  // fab  // here object is used when the event carries no data

    data class EditWorkout(val exercise : ExerciseEntity) : HomeEvent

    //    data class DeleteExercise(val exercise: ExerciseEntity): ExerciseUiEvent
}