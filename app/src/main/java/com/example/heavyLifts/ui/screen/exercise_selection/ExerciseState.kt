package com.example.heavyLifts.ui.screen.exercise_selection

import com.example.heavyLifts.data.entity.ExerciseEntity

data class ExerciseState (
    val exercises : List<ExerciseEntity> = emptyList(),
    val searchQuery :String="",
    val selectedCategory : String?=null,
    val isSearching : Boolean=false,
)