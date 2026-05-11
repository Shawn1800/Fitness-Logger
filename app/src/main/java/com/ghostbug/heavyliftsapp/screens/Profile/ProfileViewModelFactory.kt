package com.ghostbug.heavyliftsapp.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import com.ghostbug.heavyliftsapp.screens.profile.ProfileViewModel

class ProfileViewModelFactory(
    private val userProfileRepository: UserProfileRepository,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileViewModel(userProfileRepository, authRepository) as T
    }
}