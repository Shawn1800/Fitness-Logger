package com.ghostbug.heavyliftsapp.ui.screen.exercise_selection

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity

sealed interface ExerciseUiEvent {
    data class  AddExercise(val exercise: ExerciseEntity): ExerciseUiEvent  //this is used to go  to the logworkoutscreen
    object NavigateBack: ExerciseUiEvent

    data class SendSnackbar(val message:String) : ExerciseUiEvent



}