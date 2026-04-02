package com.ghostbug.heavyLifts.data.repository


import com.ghostbug.heavyLifts.data.domain.ExerciseEntity

interface ExerciseRepository {

    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    suspend fun getAllExercises(): List<ExerciseEntity>
    suspend fun searchExercises(query: String): List<ExerciseEntity>
    suspend fun getExerciseByCategory(category: String): List<ExerciseEntity>
    suspend fun searchExerciseByCategory(query: String, category: String): List<ExerciseEntity>
}
