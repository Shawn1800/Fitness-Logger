package com.example.HeavyLifts.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity("workout_entry",
foreignKeys =[
    ForeignKey(
        entity = ExerciseEntity::class,
        parentColumns = ["exercise_id"],
        childColumns = ["exercise_id"],
        onDelete = ForeignKey.CASCADE
    )
],
indices=[Index("exercise_id")]
)
data class WorkoutEntryEntity(

    @PrimaryKey(autoGenerate = true)
    val entryId : Int = 0,

    @ColumnInfo(name = "exercise_id")
    val exerciseId : Int,

    @ColumnInfo("weight")
    val weight : Double,

    @ColumnInfo("reps")
    val reps : Int,

    @ColumnInfo("date")
    val date  : Long,

    @ColumnInfo("sets")
    val sets : Int

)