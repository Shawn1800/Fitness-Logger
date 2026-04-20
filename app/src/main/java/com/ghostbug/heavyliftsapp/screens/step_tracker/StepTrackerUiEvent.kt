package com.ghostbug.heavyliftsapp.screens.step_tracker

import com.ghostbug.heavyliftsapp.data.domain.ActivityGoals
import com.ghostbug.heavyliftsapp.data.domain.DailyActivity

sealed interface StepTrackerUiEvent {
    data class ShowSnackbar(val message: String) : StepTrackerUiEvent
    data object GoalSavedSuccess : StepTrackerUiEvent
}