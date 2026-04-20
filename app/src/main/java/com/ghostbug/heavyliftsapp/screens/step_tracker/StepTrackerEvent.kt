package com.ghostbug.heavyliftsapp.screens.step_tracker

sealed interface StepTrackerEvent {
    data object LoadData : StepTrackerEvent
    data object RefreshToday : StepTrackerEvent
    data object OpenGoalEditor : StepTrackerEvent
    data object CloseGoalEditor : StepTrackerEvent
    data object SaveGoal : StepTrackerEvent
    data class OnStepGoalChanged(val value: String) : StepTrackerEvent
    data class OnCalorieGoalChanged(val value: String) : StepTrackerEvent
    data class LoadHistory(val days: Int) : StepTrackerEvent
}