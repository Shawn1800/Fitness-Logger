package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.WorkoutEntryEntity
import com.ghostbug.heavyliftsapp.data.domain.WorkoutWithExercise

interface WorkoutRepository {
    suspend fun insertWorkoutEntry(entries: List<WorkoutEntryEntity>)
    
    suspend fun getWorkoutByDate(date: Long): List<WorkoutWithExercise>
    
    suspend fun deleteSetById(id: Long)
    
    suspend fun getWorkoutByExerciseAndDate(exerciseId: Long, date: Long): List<WorkoutEntryEntity>

    suspend fun getWorkoutsByExercise(exerciseId: Long): List<WorkoutEntryEntity>
}
