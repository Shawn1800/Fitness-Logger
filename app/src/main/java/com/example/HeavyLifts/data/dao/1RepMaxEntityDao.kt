package com.example.HeavyLifts.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.HeavyLifts.data.entity.OneRepMaxEntity
import kotlinx.coroutines.flow.Flow


@Dao interface OneRepMaxEntityDao {

    @Insert(onConflict= OnConflictStrategy.REPLACE)
    suspend fun insert(oneRepMaxEntity : OneRepMaxEntity)

    @Query("SELECT change_percent FROM  one_rep_max WHERE exercise_id = :exerciseId AND date = :date" )
    suspend fun  getChangePercentageForDate(exerciseId: Int,date:Long) :Double?

    @Query("SELECT * FROM one_rep_max WHERE date = :date")
    fun getOneRepMaxForDate(date: Long): Flow<List<OneRepMaxEntity>>

    @Query("SELECT * FROM one_rep_max WHERE exercise_id = :exerciseId AND date < :date ORDER BY date DESC LIMIT 1")
    suspend fun getPrevious(exerciseId: Int, date: Long) : OneRepMaxEntity?

    @Query("SELECT * FROM one_rep_max WHERE exercise_id = :exerciseId ORDER BY date DESC LIMIT 1 ")
    suspend fun getLatest(exerciseId: Int) : OneRepMaxEntity?

    @Query ("DELETE FROM one_rep_max WHERE exercise_id = :exerciseId AND date = :date  ")
    suspend fun deleteOneRepMax(exerciseId:Int ,date:Long)


}