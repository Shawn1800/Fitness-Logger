package com.example.demo103.di

import android.content.Context
import com.example.demo103.data.UseCase.OneRepMaxUseCase
import com.example.demo103.data.dao.ExerciseDao
import com.example.demo103.data.dao.OneRepMaxEntityDao
import com.example.demo103.data.dao.WorkoutEntryEntityDao
import com.example.demo103.data.db.ExerciseDatabase
import com.example.demo103.data.repository.ExerciseRepository
import com.example.demo103.data.repository.OneRepMaxRepository
import com.example.demo103.data.repository.WorkoutRepository

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