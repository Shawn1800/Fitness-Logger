package com.example.heavyLifts.ui.screen.exercise_selection

import com.example.heavyLifts.data.entity.ExerciseEntity

sealed interface ExerciseUiEvent {
    data class  AddExercise(val exercise: ExerciseEntity): ExerciseUiEvent  //this is used to go  to the logworkoutscreen
    object NavigateBack: ExerciseUiEvent

    data class SendSnackbar(val message:String) : ExerciseUiEvent

//    data object NavToLogScreen: ExerciseUiEvent

//    data class OnClickExerciseOnSearch(val exercise: ExerciseEntity): ExerciseUiEvent

}