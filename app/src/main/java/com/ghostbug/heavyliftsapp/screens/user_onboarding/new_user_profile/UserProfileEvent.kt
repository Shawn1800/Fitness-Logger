package com.ghostbug.heavyliftsapp.screens.user_onboarding.new_user_profile

import HeightUnit
import WeightUnit
import com.ghostbug.heavyliftsapp.data.domain.Gender
import com.ghostbug.heavyliftsapp.screens.home.HomeEvent
import kotlin.time.Instant

sealed interface UserProfileEvent {
    data class OnUserNameChanged(val userName: String) : UserProfileEvent
    data class OnAgeChanged(val age: String) : UserProfileEvent
    data class OnHeightChanged(val height: String) : UserProfileEvent
    data class OnUserWeightChanged(val userWeight: String) : UserProfileEvent
    data class OnGenderChanged(val gender: Gender) : UserProfileEvent
    data class OnCityChanged(val city: String) : UserProfileEvent
    data class OnCountryChanged(val country: String) : UserProfileEvent
    data class OnDobChanged(val dob: Instant) : UserProfileEvent
    data class OnProfilePicChanged(val profilePic: String) : UserProfileEvent
    object  NavtoScreen2 : UserProfileEvent
    object NavtoScreen3 : UserProfileEvent

    object OnSubmitProfile : UserProfileEvent

    data class message(val message: String): UserProfileEvent
    data class OnHeightUnitChanged(val unit: HeightUnit) : UserProfileEvent
    data class OnWeightUnitChanged(val unit: WeightUnit) : UserProfileEvent
}