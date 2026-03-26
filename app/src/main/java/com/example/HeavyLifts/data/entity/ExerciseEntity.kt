package com.example.HeavyLifts.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="exercises")
data class ExerciseEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Int = 0,

    @ColumnInfo(name="exercise_name")
    val exerciseName: String,

    @ColumnInfo(name="category")
    val category:String

)