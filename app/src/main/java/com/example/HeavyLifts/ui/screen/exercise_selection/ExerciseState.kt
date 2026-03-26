package com.example.HeavyLifts.ui.screen.exercise_selection

import com.example.HeavyLifts.data.entity.ExerciseEntity

data class ExerciseState (
    val exercises : List<ExerciseEntity> = emptyList(),
    val searchQuery :String="",
    val selectedCategory : String?=null,
    val isSearching : Boolean=false,
)