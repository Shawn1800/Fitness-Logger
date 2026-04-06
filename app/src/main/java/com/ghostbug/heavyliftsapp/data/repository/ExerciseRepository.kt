package com.ghostbug.heavyliftsapp.data.repository

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity

interface ExerciseRepository {
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    
    suspend fun getAllExercises(): List<ExerciseEntity>
    
    suspend fun searchExercises(query: String): List<ExerciseEntity>
    
    suspend fun getExerciseByCategory(category: String): List<ExerciseEntity>

    suspend fun searchExerciseByCategory(query: String, category: String): List<ExerciseEntity>
}
