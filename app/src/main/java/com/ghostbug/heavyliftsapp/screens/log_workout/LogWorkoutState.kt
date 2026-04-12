package com.ghostbug.heavyliftsapp.screens.log_workout

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity

data class LogWorkoutState (
   val currentWeight:String ="",// we use String cause doubles can crash ,we convert later
   val currentReps:String="",
   val selectedExerciseId:Long?=null,
//   val isLoading: Boolean=false,
   val sets:List<WorkoutEntryEntity> = emptyList(),
   val exercise : ExerciseEntity? = null,
   val editingWeights: Map<Long, String> = emptyMap(),
   val editingReps: Map<Long, String> = emptyMap(),
   val dateMillis : Long = 0L,
   val isReadOnly: Boolean = false

)
