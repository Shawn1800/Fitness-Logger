package com.ghostbug.heavyLifts.ui.screen.signIn


import SignInViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyLifts.data.repository.AuthRepository

class SignInViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(authRepository) as T
    }
}