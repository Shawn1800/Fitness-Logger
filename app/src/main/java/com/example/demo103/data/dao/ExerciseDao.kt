package com.example.demo103.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demo103.data.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE exercise_name LIKE '%' || :query || '%'")
    fun searchExercises(query: String): Flow<List<ExerciseEntity>> //edit flow

    @Query("SELECT * FROM exercises WHERE category = :category")
    fun getExerciseByCategory(category :String  ): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE exercise_name LIKE '%' || :query || '%' AND category = :category")
    fun searchExerciseByCategory(
        query:String,
        category :String
    ): Flow<List<ExerciseEntity>>

}
