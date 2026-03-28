package com.example.heavyLifts.data.repository

import com.example.heavyLifts.data.entity.WorkoutEntryEntity
import com.example.heavyLifts.data.dao.WorkoutEntryEntityDao
import com.example.heavyLifts.data.entity.WorkoutWithExercise
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutEntryEntityDao: WorkoutEntryEntityDao) {
    suspend fun insertWorkoutEntry(entry: List<WorkoutEntryEntity>){
        workoutEntryEntityDao.insertWorkoutEntry(entry)
    }

    fun getWorkoutByDate(date:Long): Flow<List<WorkoutWithExercise>> {
        return workoutEntryEntityDao.getWorkoutByDate(date)
    }

    suspend fun deleteSetById (entryId :Int ){
        workoutEntryEntityDao. deleteSetById(entryId)
    }

    suspend fun getWorkoutByExerciseAndDate (exerciseId: Int, date:Long ) : List<WorkoutEntryEntity> {
        return workoutEntryEntityDao.getWorkoutByExerciseAndDate(exerciseId,date)
    }

//    suspend fun  deleteWorkoutByExerciseAndDate(exerciseId: Int,date: Long) {
//        workoutEntryEntityDao.deleteWorkoutByExerciseAndDate(exerciseId,date)
//    }

}