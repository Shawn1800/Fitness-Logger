package com.ghostbug.heavyLifts.ui.screen.exercise_selection

import com.ghostbug.heavyLifts.data.domain.ExerciseEntity


data class ExerciseState (
    val exercises : List<ExerciseEntity> = emptyList(),
    val searchQuery :String="",
    val selectedCategory : String?=null,
    val isSearching : Boolean=false,
)