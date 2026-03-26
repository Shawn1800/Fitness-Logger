package com.example.HeavyLifts.ui.screen.exercise_selection

import com.example.HeavyLifts.data.entity.ExerciseEntity

sealed class ExerciseEvent {
    data class OnSearchQueryChange(val query :String ): ExerciseEvent()
    data class OnSelectCategory(val category: String?): ExerciseEvent()
    data object OnClearCategory : ExerciseEvent()
    data class OnAddExercise(val exercise: ExerciseEntity): ExerciseEvent()

//    data class OnExerciseSelected(
//        val exercise: ExerciseEntity
//    ) : ExerciseEvent()
}
