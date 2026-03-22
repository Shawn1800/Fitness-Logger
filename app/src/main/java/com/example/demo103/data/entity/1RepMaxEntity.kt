package com.example.demo103.data.entity

import android.R.attr.value
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "one_rep_max",
foreignKeys=[
    ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns= ["exerciseId"],
        childColumns=["exercise_id"],
        onDelete= ForeignKey.CASCADE
    )
],
indices=[Index("exercise_id")]
)

data class OneRepMaxEntity(

    @PrimaryKey(autoGenerate = true)
    val oneRMId: Int=0,

    @ColumnInfo(name="exercise_Id")
    val exerciseId: Int,

    @ColumnInfo(name="curr_1rm")
    val curr1RM:Double=0.0,

    @ColumnInfo(name="prev_1rm")
    val prev1RM:Double=0.0,

    @ColumnInfo(name="change_percent")
    val changePercent:Double=0.0,

    @ColumnInfo(name="date")
    val date:Long,

)