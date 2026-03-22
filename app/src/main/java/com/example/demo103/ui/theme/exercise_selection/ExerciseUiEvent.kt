package com.example.demo103.ui.theme.exercise_selection

import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.ui.theme.log_workout.LogWorkoutUiEvent

sealed interface ExerciseUiEvent {
    data class  AddExercise(val exercise: ExerciseEntity): ExerciseUiEvent  //this is used to go  to the logworkoutscreen
    object NavigateBack: ExerciseUiEvent

    data class SendSnackbar(val message:String) : ExerciseUiEvent

//    data object NavToLogScreen: ExerciseUiEvent

//    data class OnClickExerciseOnSearch(val exercise: ExerciseEntity): ExerciseUiEvent

}