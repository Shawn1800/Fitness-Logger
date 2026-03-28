package com.example.heavyLifts.di

import android.content.Context
import com.example.heavyLifts.data.UseCase.OneRepMaxUseCase
import com.example.heavyLifts.data.dao.ExerciseDao
import com.example.heavyLifts.data.dao.OneRepMaxEntityDao
import com.example.heavyLifts.data.dao.WorkoutEntryEntityDao
import com.example.heavyLifts.data.db.ExerciseDatabase
import com.example.heavyLifts.data.repository.ExerciseRepository
import com.example.heavyLifts.data.repository.OneRepMaxRepository
import com.example.heavyLifts.data.repository.WorkoutRepository

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