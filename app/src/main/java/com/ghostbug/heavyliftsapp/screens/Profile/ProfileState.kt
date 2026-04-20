package com.ghostbug.heavyliftsapp.screens.profile

import HeightUnit
import WeightUnit
import com.ghostbug.heavyliftsapp.data.domain.Gender


data class ProfileState(
    // profile data
    val userId: String = "",
    val userName: String = "",
    val email: String = "",
    val profilePic: String? = null,
    val age: Int? = null,
    val ageInputText: String = "",
    val height: Float? = null,
    val heightInputText: String = "",
    val userWeight: Float? = null,
    val weightInputText: String = "",
    val gender: Gender? = null,
    val city: String? = null,
    val country: String? = null,

    // units
    val heightUnit: HeightUnit = HeightUnit.CM,
    val weightUnit: WeightUnit = WeightUnit.KG,

    // editing
    val isEditing: Boolean = false,
    val userNameError: String? = null,

    // ui
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,

    // confirmation dialogs
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)