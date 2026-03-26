package com.example.demo103.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.demo103.data.entity.WorkoutEntryEntity
import com.example.demo103.data.entity.WorkoutWithExercise
import kotlinx.coroutines.flow.Flow

//also called appContainer
@Dao
interface WorkoutEntryEntityDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutEntry(entry: List<WorkoutEntryEntity>) //recheck

    @Transaction
    @Query("SELECT * FROM workout_entry WHERE date = :date")
    fun getWorkoutByDate(date:Long): Flow<List<WorkoutWithExercise>>

    @Query("SELECT * FROM workout_entry WHERE exercise_id = :exerciseId")
    fun getWorkoutByExercise(exerciseId: Int): Flow<List<WorkoutEntryEntity>>

    @Query("DELETE  FROM workout_entry WHERE entryId = :entryId")
    suspend fun deleteSetById(entryId:Int)

    @Query("SELECT * FROM workout_entry WHERE exercise_id = :exerciseId AND date = :date  ")
    suspend fun getWorkoutByExerciseAndDate(exerciseId: Int,date: Long) : List<WorkoutEntryEntity>


    @Query ("DELETE FROM workout_entry WHERE exercise_id = :exerciseId AND date =:date ")
    suspend fun deleteWorkoutByExerciseAndDate(exerciseId:Int,date:Long )
}