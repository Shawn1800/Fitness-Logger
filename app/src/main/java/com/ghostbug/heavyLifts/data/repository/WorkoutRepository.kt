package com.ghostbug.heavyLifts.data.repository

import com.ghostbug.heavyLifts.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyLifts.data.domain.WorkoutWithExercise

interface WorkoutRepository {
    suspend fun insertWorkoutEntry(entries: List<WorkoutEntryEntity>)
    
    suspend fun getWorkoutByDate(date: Long): List<WorkoutWithExercise>
    
    suspend fun deleteSetById(id: Int)
    
    suspend fun getWorkoutByExerciseAndDate(exerciseId: Int, date: Long): List<WorkoutEntryEntity>

    suspend fun getWorkoutsByExercise(exerciseId: Int): List<WorkoutEntryEntity>
}
