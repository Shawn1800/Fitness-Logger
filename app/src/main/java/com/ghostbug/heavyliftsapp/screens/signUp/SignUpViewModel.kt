package com.ghostbug.heavyliftsapp.screens.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import com.ghostbug.heavyliftsapp.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignUpUiEvent>()
    val uiEvent: SharedFlow<SignUpUiEvent> = _uiEvent.asSharedFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }

            is SignUpEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }

            is SignUpEvent.OnSignUpClick -> {
                signUp()
            }

            is SignUpEvent.OnNavigateToScreen1 -> {
                viewModelScope.launch {
                    _uiEvent.emit(SignUpUiEvent.NavToScreen1)
                }
            }

            SignUpEvent.OnNavigateToSignIn -> {
                viewModelScope.launch {
                    _uiEvent.emit(SignUpUiEvent.NavigateToSignIn)
                }
            }
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val success = authRepository.signUp(
                email = _state.value.email,
                password = _state.value.password
            )
            if (success) {
                checkUserProfileAndNavigate()
            } else {
                _uiEvent.emit(SignUpUiEvent.ShowError("Sign up failed"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun checkUserProfileAndNavigate() {
        val profile = userProfileRepository.getOwnProfile()
        if (profile == null) {
            _uiEvent.emit(SignUpUiEvent.NavigateToOnboarding)
        } else {
            _uiEvent.emit(SignUpUiEvent.NavigateToHome)
        }
    }
}

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

sealed class SignUpUiEvent {
    object NavToScreen1 : SignUpUiEvent()
    object NavigateToSignIn : SignUpUiEvent()
    object NavigateToOnboarding : SignUpUiEvent()
    object NavigateToHome : SignUpUiEvent()
    data class ShowError(val message: String) : SignUpUiEvent()
}
