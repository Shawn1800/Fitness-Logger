package com.ghostbug.heavyliftsapp.ui.screen.signIn


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository

class SignInViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(authRepository) as T
    }
}