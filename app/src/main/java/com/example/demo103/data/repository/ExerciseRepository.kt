package com.example.demo103.data.repository

import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.dao.ExerciseDao
import com.example.demo103.data.dao.WorkoutEntryEntityDao
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutEntryEntityDao:  WorkoutEntryEntityDao
) {
    fun getAllExercises(): Flow<List<ExerciseEntity>> {
        return  exerciseDao.getAllExercises()
    }
    fun searchExercises(query: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.searchExercises(query)
    }
    fun getExerciseByCategory(category :String  ): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExerciseByCategory(category)
    }
    fun searchExerciseByCategory(query: String, category: String) : Flow<List<ExerciseEntity>>{
        return exerciseDao.searchExerciseByCategory(query,category)
    }
}