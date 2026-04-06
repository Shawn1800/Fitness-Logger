package com.ghostbug.heavyliftsapp.ui.screen.signUp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostbug.heavyliftsapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SignUpUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.OnEmailChange -> {
                _state.update { it.copy(email = event.email) }
            }
            is SignUpEvent.OnPasswordChange -> {
                _state.update { it.copy(password = event.password) }
            }
            is SignUpEvent.OnSignUpClick -> signUp()
            is SignUpEvent.OnNavigateToSignIn -> {
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
                _uiEvent.emit(SignUpUiEvent.NavigateToSignIn)
            } else {
                _uiEvent.emit(SignUpUiEvent.ShowError("Sign up failed"))
            }
            _state.update { it.copy(isLoading = false) }
        }
    }
}
data class SignUpState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

sealed class SignUpUiEvent {
    object NavigateToSignIn : SignUpUiEvent()
    data class ShowError(val message: String) : SignUpUiEvent()
}