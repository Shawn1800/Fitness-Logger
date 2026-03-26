package com.example.demo103.ui.screen.home

import com.example.demo103.data.entity.ExerciseEntity


sealed interface HomeUiEvent {
    data object NavigateToExerciseSelection : HomeUiEvent
    //    data class EditExercise(val exercise: ExerciseEntity): ExerciseUiEvent
    data class  NavigateToLogWorkout  (val exercise: ExerciseEntity) : HomeUiEvent
}