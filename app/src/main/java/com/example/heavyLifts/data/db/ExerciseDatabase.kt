package com.example.heavyLifts.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.heavyLifts.data.entity.ExerciseEntity
import com.example.heavyLifts.data.entity.WorkoutEntryEntity
import com.example.heavyLifts.data.entity.OneRepMaxEntity
import com.example.heavyLifts.data.dao.ExerciseDao
import com.example.heavyLifts.data.dao.WorkoutEntryEntityDao
import com.example.heavyLifts.data.dao.OneRepMaxEntityDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ExerciseEntity::class, WorkoutEntryEntity::class, OneRepMaxEntity::class],
    version = 4,
    exportSchema = false
)

abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract  fun workoutEntryEntityDao(): WorkoutEntryEntityDao

    abstract fun oneRepMaxEntityDao(): OneRepMaxEntityDao
    companion object {
        @Volatile
        private var INSTANCE: ExerciseDatabase? = null
        private val defaultExercises = listOf(
            // CHEST
            ExerciseEntity(exerciseName = "Bench Press", category = "chest"),
            ExerciseEntity(exerciseName = "Incline Bench Press", category = "chest"),
            ExerciseEntity(exerciseName = "Decline Bench Press", category = "chest"),
            ExerciseEntity(exerciseName = "Dumbbell Press", category = "chest"),
            ExerciseEntity(exerciseName = "Incline Dumbbell Press", category = "chest"),
            ExerciseEntity(exerciseName = "Chest Fly (Machine)", category = "chest"),
            ExerciseEntity(exerciseName = "Cable Fly", category = "chest"),
            ExerciseEntity(exerciseName = "Dips (Chest)", category = "chest"),
            ExerciseEntity(exerciseName = "Push Ups", category = "chest"),
            ExerciseEntity(exerciseName = "Diamond Push Ups", category = "triceps"),

            // BACK
            ExerciseEntity(exerciseName = "Deadlift", category = "back"),
            ExerciseEntity(exerciseName = "Pull Ups", category = "back"),
            ExerciseEntity(exerciseName = "Lat Pulldown", category = "back"),
            ExerciseEntity(exerciseName = "Barbell Row", category = "back"),
            ExerciseEntity(exerciseName = "Seated Cable Row", category = "back"),
            ExerciseEntity(exerciseName = "T-Bar Row", category = "back"),
            ExerciseEntity(exerciseName = "Single Arm Dumbbell Row", category = "back"),
            ExerciseEntity(exerciseName = "Face Pull", category = "back"),

            // LEGS
            ExerciseEntity(exerciseName = "Squat", category = "legs"),
            ExerciseEntity(exerciseName = "Leg Press", category = "legs"),
            ExerciseEntity(exerciseName = "Lunges", category = "legs"),
            ExerciseEntity(exerciseName = "Bulgarian Split Squat", category = "legs"),
            ExerciseEntity(exerciseName = "Leg Extension", category = "legs"),
            ExerciseEntity(exerciseName = "Hamstring Curl", category = "legs"),
            ExerciseEntity(exerciseName = "Romanian Deadlift", category = "legs"),
            ExerciseEntity(exerciseName = "Calf Raises", category = "legs"),

            // SHOULDERS
            ExerciseEntity(exerciseName = "Overhead Press", category = "shoulders"),
            ExerciseEntity(exerciseName = "Dumbbell Shoulder Press", category = "shoulders"),
            ExerciseEntity(exerciseName = "Lateral Raises", category = "shoulders"),
            ExerciseEntity(exerciseName = "Front Raises", category = "shoulders"),
            ExerciseEntity(exerciseName = "Rear Delt Fly", category = "shoulders"),
            ExerciseEntity(exerciseName = "Arnold Press", category = "shoulders"),
            ExerciseEntity(exerciseName = "Cable Lateral Raise", category = "shoulders"),

            // BICEPS
            ExerciseEntity(exerciseName = "Barbell Curl", category = "biceps"),
            ExerciseEntity(exerciseName = "Dumbbell Curl", category = "biceps"),
            ExerciseEntity(exerciseName = "Hammer Curl", category = "biceps"),
            ExerciseEntity(exerciseName = "Preacher Curl", category = "biceps"),
            ExerciseEntity(exerciseName = "Cable Curl", category = "biceps"),

            // TRICEPS
            ExerciseEntity(exerciseName = "Tricep Pushdown", category = "triceps"),
            ExerciseEntity(exerciseName = "Overhead Tricep Extension", category = "triceps"),
            ExerciseEntity(exerciseName = "Skull Crushers", category = "triceps"),
            ExerciseEntity(exerciseName = "Close Grip Bench Press", category = "triceps"),
            ExerciseEntity(exerciseName = "Dips (Triceps)", category = "triceps"),

            // CORE
//            ExerciseEntity(exerciseName = "Hanging Leg Raise", category = "core"),
//            ExerciseEntity(exerciseName = "Cable Crunch", category = "core"),
//            ExerciseEntity(exerciseName = "Ab Crunch Machine", category = "core"),
//            ExerciseEntity(exerciseName = "Russian Twist", category = "core"),
//            ExerciseEntity(exerciseName = "Plank", category = "core")
        )

        fun getInstance(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    "exercise_db"
                )
                    .fallbackToDestructiveMigration() // For development: clears data on schema change
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



