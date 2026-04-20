package com.ghostbug.heavyliftsapp.screens.profile

import HeightUnit
import WeightUnit
import com.ghostbug.heavyliftsapp.data.domain.Gender
import kotlin.time.Instant

sealed interface ProfileEvent {
    data object LoadProfile : ProfileEvent
    data object StartEditing : ProfileEvent
    data object CancelEditing : ProfileEvent
    data object SaveProfile : ProfileEvent
    data object Logout : ProfileEvent
    data object ShowLogoutDialog : ProfileEvent
    data object HideLogoutDialog : ProfileEvent
    data object ShowDeleteDialog : ProfileEvent
    data object HideDeleteDialog : ProfileEvent
    data object DeleteAccount : ProfileEvent
    data class OnUserNameChanged(val value: String) : ProfileEvent
    data class OnAgeChanged(val value: String) : ProfileEvent
    data class OnHeightChanged(val value: String) : ProfileEvent
    data class OnUserWeightChanged(val value: String) : ProfileEvent
    data class OnGenderChanged(val value: Gender) : ProfileEvent
    data class OnCityChanged(val value: String) : ProfileEvent
    data class OnCountryChanged(val value: String) : ProfileEvent
    data class OnHeightUnitChanged(val value: HeightUnit) : ProfileEvent
    data class OnWeightUnitChanged(val value: WeightUnit) : ProfileEvent
    data class OnProfilePicChanged(val value: String) : ProfileEvent
    data class OnDobChanged(val value: Instant) : ProfileEvent
    data class message(val value: String) : ProfileEvent
}