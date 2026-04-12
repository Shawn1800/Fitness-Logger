package com.ghostbug.heavyliftsapp.screens.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository

class SignUpViewModelFactory(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignUpViewModel(authRepository, userProfileRepository) as T
    }
}