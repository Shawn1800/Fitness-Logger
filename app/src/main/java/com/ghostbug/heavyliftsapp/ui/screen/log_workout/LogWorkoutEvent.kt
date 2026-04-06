package com.ghostbug.heavyliftsapp.ui.screen.log_workout

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity

sealed interface LogWorkoutEvent {
    data class  OnAddingSets(val exerciseId : Int) : LogWorkoutEvent
    data class UpdateWeight (val id:Int, val weight : String): LogWorkoutEvent
    data class UpdateReps (val id:Int ,val reps : String): LogWorkoutEvent
    data class DeleteSet (val id : Int): LogWorkoutEvent
    data class SetExercise (val exercise : ExerciseEntity, val dateMillis :Long) : LogWorkoutEvent
    data object SaveWorkout : LogWorkoutEvent

}