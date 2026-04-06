package com.ghostbug.heavyliftsapp.ui.screen.exercise_selection

import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity

sealed class ExerciseEvent {
    data class OnSearchQueryChange(val query :String ): ExerciseEvent()
    data class OnSelectCategory(val category: String?): ExerciseEvent()
    data object OnClearCategory : ExerciseEvent()
    data class OnAddExercise(val exercise: ExerciseEntity): ExerciseEvent()

}
