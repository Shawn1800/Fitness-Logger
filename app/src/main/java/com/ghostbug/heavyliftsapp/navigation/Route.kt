package com.ghostbug.heavyliftsapp.navigation

import androidx.navigation3.runtime.NavKey
import com.ghostbug.heavyliftsapp.data.domain.ExerciseEntity
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object SignInScreen:Route ,NavKey

    @Serializable
    data object SignUpScreen :Route ,NavKey

    @Serializable
    data object HomeScreen: Route , NavKey

    @Serializable
    data object ExerciseScreen : Route , NavKey
    @Serializable
    data class LogWorkoutScreen (val exercise: ExerciseEntity, val dateMillis: Long) : Route , NavKey

}