package com.example.HeavyLifts.ui.screen.log_workout

sealed interface LogWorkoutUiEvent {
    data class SendSnackbar(val message:String) : LogWorkoutUiEvent
    object  NavBackToHome: LogWorkoutUiEvent
}