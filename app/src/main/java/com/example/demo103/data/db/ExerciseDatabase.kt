package com.example.demo103.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.demo103.data.entity.ExerciseEntity
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.dao.ExerciseDao
import com.example.demo103.data.dao.WorkoutEntryEntityDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ExerciseEntity::class, WorkoutEntryEntity::class],
    version = 2,
    exportSchema = false
)

abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract  fun workoutEntryEntityDao(): WorkoutEntryEntityDao
    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null
        private val defaultExercises = listOf(
            ExerciseEntity(exerciseName = "Bench Press", category = "chest"),
            ExerciseEntity(exerciseName = "Deadlift", category = "back"),
            ExerciseEntity(exerciseName = "Squats", category = "legs"),
            ExerciseEntity(exerciseName = "Overhead Press", category = "shoulders"),
            ExerciseEntity(exerciseName = "Pull Ups", category = "back"),
            ExerciseEntity(exerciseName = "Dumbbell Press", category = "chest"),
            ExerciseEntity(exerciseName = "Bicep Curls", category = "arms"),
            ExerciseEntity(exerciseName = "Preacher Curls", category = "arms")
        )
        fun getInstance(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.exerciseDao().insertExercises(defaultExercises)
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}



