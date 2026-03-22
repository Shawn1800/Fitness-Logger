package com.example.demo103.ui.theme.home

import com.example.demo103.data.entity.ExerciseEntity
import java.time.LocalDate

sealed interface HomeEvent {
    data class OnDateSelected(val date: LocalDate) : HomeEvent // data class is used when the event carries info
    object OnAddWorkoutClick : HomeEvent  // fab  // here object is used when the event carries no data

    data class EditWorkout(val exercise : ExerciseEntity) : HomeEvent

    //    data class DeleteExercise(val exercise: ExerciseEntity): ExerciseUiEvent
}