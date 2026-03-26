package com.example.demo103.ui.screen.exercise_selection

import com.example.demo103.data.entity.ExerciseEntity

sealed interface ExerciseUiEvent {
    data class  AddExercise(val exercise: ExerciseEntity): ExerciseUiEvent  //this is used to go  to the logworkoutscreen
    object NavigateBack: ExerciseUiEvent

    data class SendSnackbar(val message:String) : ExerciseUiEvent

//    data object NavToLogScreen: ExerciseUiEvent

//    data class OnClickExerciseOnSearch(val exercise: ExerciseEntity): ExerciseUiEvent

}