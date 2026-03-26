package com.example.HeavyLifts.ui.screen.log_workout

import com.example.HeavyLifts.data.entity.ExerciseEntity
import com.example.HeavyLifts.data.entity.WorkoutEntryEntity

data class LogWorkoutState (
   val currentWeight:String ="",// we use String cause doubles can crash ,we convert later
   val currentReps:String="",
   val selectedExerciseId:Int?=null,
//   val isLoading: Boolean=false,
   val sets:List<WorkoutEntryEntity> = emptyList(),
   val exercise : ExerciseEntity? = null,
   val editingWeights: Map<Int, String> = emptyMap(),
   val editingReps: Map<Int, String> = emptyMap(),
   val dateMillis : Long = 0L
)
