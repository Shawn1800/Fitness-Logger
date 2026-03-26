package com.example.HeavyLifts.di

import android.content.Context
import com.example.HeavyLifts.data.UseCase.OneRepMaxUseCase
import com.example.HeavyLifts.data.dao.ExerciseDao
import com.example.HeavyLifts.data.dao.OneRepMaxEntityDao
import com.example.HeavyLifts.data.dao.WorkoutEntryEntityDao
import com.example.HeavyLifts.data.db.ExerciseDatabase
import com.example.HeavyLifts.data.repository.ExerciseRepository
import com.example.HeavyLifts.data.repository.OneRepMaxRepository
import com.example.HeavyLifts.data.repository.WorkoutRepository

class AppContainer (context: Context){

    val database: ExerciseDatabase =ExerciseDatabase.getInstance(context)
    val exerciseDao : ExerciseDao = database.exerciseDao()
    val workoutDao: WorkoutEntryEntityDao = database.workoutEntryEntityDao()

    val oneRepMaxDao : OneRepMaxEntityDao = database.oneRepMaxEntityDao()

    val exerciseRepository= ExerciseRepository(exerciseDao,workoutDao)

    val workoutRepository = WorkoutRepository(workoutDao)

    val  oneRepMaxRepository = OneRepMaxRepository(oneRepMaxEntityDao = oneRepMaxDao, workoutEntryEntityDao = workoutDao)

    val oneRepMaxUseCase = OneRepMaxUseCase(oneRepMaxRepository, workoutRepository)
}