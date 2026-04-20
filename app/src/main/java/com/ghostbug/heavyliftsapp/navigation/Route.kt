package com.ghostbug.heavyliftsapp.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object SignInScreen : Route, NavKey

    @Serializable
    data object SignUpScreen : Route, NavKey

    @Serializable
    data object HomeScreen : Route, NavKey

    @Serializable
    data object ExerciseScreen : Route, NavKey

    @Serializable
    data class LogWorkoutScreen(val exerciseId: Long, val dateMillis: Long) : Route, NavKey

    @Serializable
    data object UserProfileScreen1 : Route, NavKey

    @Serializable
    data object UserProfileScreen2 : Route, NavKey

    @Serializable
    data object UserProfileScreen3 : Route, NavKey

    @Serializable
    data object StepTrackerScreen : Route, NavKey

    @Serializable
    data object ProfileScreen : Route {

    }

    @Serializable
    data object LeaderBoards : Route {

    }
}