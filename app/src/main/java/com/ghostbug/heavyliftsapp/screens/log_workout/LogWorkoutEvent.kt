package com.ghostbug.heavyliftsapp.screens.log_workout

sealed interface LogWorkoutEvent {
    data class  OnAddingSets(val exerciseId : Long) : LogWorkoutEvent
    data class UpdateWeight (val id:Long, val weight : String): LogWorkoutEvent
    data class UpdateReps (val id:Long ,val reps : String): LogWorkoutEvent
    data class DeleteSet (val id : Long): LogWorkoutEvent
    data class SetExerciseById (val exerciseId : Long, val dateMillis :Long) : LogWorkoutEvent
    data object SaveWorkout : LogWorkoutEvent

}