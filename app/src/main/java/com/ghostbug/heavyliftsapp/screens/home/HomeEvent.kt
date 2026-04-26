package com.ghostbug.heavyliftsapp.screens.home

import kotlinx.datetime.LocalDate


sealed interface HomeEvent {
    data class OnDateSelected(val date: LocalDate) : HomeEvent
    object OnAddWorkoutClick : HomeEvent
    data class EditWorkout(val exerciseId: Long) : HomeEvent
    object RefreshWorkouts : HomeEvent

    data class Message(val message: String): HomeEvent
    data object GetSteps: HomeEvent

    data class OnStepsChanged (val steps: Long): HomeEvent


}