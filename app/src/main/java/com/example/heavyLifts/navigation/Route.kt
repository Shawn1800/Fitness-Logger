package com.example.heavyLifts.navigation

import androidx.navigation3.runtime.NavKey
import com.example.heavyLifts.data.entity.ExerciseEntity
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object LogInScreen:Route ,NavKey

    @Serializable
    data object SignUpScreen :Route ,NavKey

    @Serializable
    data object HomeScreen: Route , NavKey

    @Serializable
    data object ExerciseScreen : Route , NavKey

    @Serializable
    data class LogWorkoutScreen (val exercise: ExerciseEntity, val dateMillis: Long) : Route , NavKey

}