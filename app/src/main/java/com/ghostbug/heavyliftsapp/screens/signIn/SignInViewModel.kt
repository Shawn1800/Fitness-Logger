package com.ghostbug.heavyliftsapp.screens.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignInUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is SignInEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }

            is SignInEvent.OnSignInClick -> signIn()
            is SignInEvent.OnNavigateToSignUp -> {
                viewModelScope.launch {
                    _uiEvent.emit(SignInUiEvent.NavigateToSignUp)
                }

            }

            is SignInEvent.OnForgotPasswordClick -> resetPassword()

            is SignInEvent.OnGoogleSignInResult -> {
                signInWithGoogle(event.idToken,event.rawNonce)
            }

        }
    }

    private fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val success = authRepository.signIn(
                email = _state.value.email,
                password = _state.value.password
            )
            if (success) {
                checkUserProfileAndNavigate()
            } else {
                _uiEvent.emit(SignInUiEvent.ShowError("Invalid email or password"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }


    private fun resetPassword() {
        viewModelScope.launch {
            val email = _state.value.email
            if (email.isBlank()) {
                _uiEvent.emit(SignInUiEvent.ShowError("Please enter your email first"))
                return@launch
            }

            _state.update { it.copy(isLoading = true) }
            val success = authRepository.sendPasswordResetEmail(email)

            if (success) {
                _uiEvent.emit(SignInUiEvent.ShowSuccess("Reset link sent to your email"))
            } else {
                _uiEvent.emit(SignInUiEvent.ShowError("Failed to send reset email"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }


    private fun signInWithGoogle(idToken: String, rawNonce: String) {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                val success = authRepository.signInWithGoogle(idToken,rawNonce)
                if (success) {
                    checkUserProfileAndNavigate()
                } else {
                    _uiEvent.emit(SignInUiEvent.ShowError("Google Sign-In Failed"))
                }
                _state.update { it.copy(isLoading = false) }
            }
        }

    private suspend fun checkUserProfileAndNavigate() {
        val profile = userProfileRepository.getOwnProfile()
        if (profile == null) {
            _uiEvent.emit(SignInUiEvent.NavigateToOnboarding)
        } else {
            _uiEvent.emit(SignInUiEvent.NavigateToHome)
        }
    }
}






data class SignInState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

sealed class SignInUiEvent {
    object NavigateToHome : SignInUiEvent()
    object NavigateToSignUp : SignInUiEvent()
    object NavigateToOnboarding : SignInUiEvent()
    data class ShowError(val message: String) : SignInUiEvent()
    data class ShowSuccess(val message: String) : SignInUiEvent()
}
