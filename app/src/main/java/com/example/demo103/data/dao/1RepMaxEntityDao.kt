//package com.example.demo103.data.dao
//
//import android.health.connect.datatypes.units.Percentage
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
//import androidx.room.Upsert
//
//
//@Dao interface OneRepMaxEntityDao {
//
//    @Insert
//    suspend fun insert(oneRepMax : OneRepMaxEntityDao)
//
//    @Query("SELECT * FROM  one_rep_max WHERE exercise_Id = :exerciseId AND date = :date AND  change_percent =:changePercentage " )
//    fun  getChangePercentaqe(exerciseId: Int,date:Long,changePercentage: Double)
//
//}