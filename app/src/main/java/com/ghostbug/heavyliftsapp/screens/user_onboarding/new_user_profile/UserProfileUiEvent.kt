package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import com.ghostbug.heavyliftsapp.screens.log_workout.LogWorkoutUiEvent

sealed interface UserProfileUiEvent {
    object NavToHome: UserProfileUiEvent
    object NavToScreen2: UserProfileUiEvent
    object NavToScreen3: UserProfileUiEvent
    data class SendSnackbar(val message:String) : UserProfileUiEvent
}