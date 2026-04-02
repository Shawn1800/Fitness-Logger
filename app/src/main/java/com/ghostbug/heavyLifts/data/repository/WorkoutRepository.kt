package com.ghostbug.heavyLifts.data.repository


import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyLifts.data.domain.WorkoutWithExercise

import kotlinx.coroutines.flow.Flow

interface WorkoutRepository{
    suspend fun insertWorkoutEntry(entry: List<WorkoutEntryEntity>)
    suspend fun getWorkoutByDate(date:Long): List<WorkoutWithExercise>
    suspend fun deleteSetById (id :Int )
    suspend fun getWorkoutByExerciseAndDate (exerciseId: Int, date:Long ) : List<WorkoutEntryEntity>

}