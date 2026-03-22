package com.example.demo103.ui.theme.log_workout

import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.entity.WorkoutWithExercise

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
