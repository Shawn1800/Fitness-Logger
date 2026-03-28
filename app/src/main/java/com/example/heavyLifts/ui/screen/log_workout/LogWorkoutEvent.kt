package com.example.heavyLifts.ui.screen.log_workout

import com.example.heavyLifts.data.entity.ExerciseEntity

sealed interface LogWorkoutEvent {
    data class  OnAddingSets(val exerciseId : Int) : LogWorkoutEvent
    data class UpdateWeight (val entryId:Int, val weight : String): LogWorkoutEvent
    data class UpdateReps (val entryId:Int ,val reps : String): LogWorkoutEvent
    data class DeleteSet (val entryId : Int): LogWorkoutEvent
    data class SetExercise (val exercise : ExerciseEntity, val dateMillis :Long) : LogWorkoutEvent
    data object SaveWorkout : LogWorkoutEvent

}