package com.example.demo103.ui.screen.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo103.data.repository.AuthRepository
import com.example.demo103.ui.screen.signIn.AuthEvent.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    init {
        checkUserStatus()
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is LogIn -> performAuthAction {
                repository.login(_state.value.email, _state.value.password)
            }

            is SignUp -> {
                val validationError = validateInput()
                if (validationError != null) {
                    _state.update { it.copy(errorMessage = validationError) }
                } else {
                    viewModelScope.launch {
                        _state.update { it.copy(isLoading = true, errorMessage = null) }
                        repository.signup(_state.value.email, _state.value.password)
                            .onSuccess {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        isWaitingForVerification = true
                                    )
                                }
                            }
                            .onFailure { e ->
                                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                            }
                    }
                }
            }

            is GoogleSignIn -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, errorMessage = null) }
                    repository.signInWithGoogle(event.idToken)
                        .onSuccess {
                            _state.update { it.copy(isLoading = false, isLoggedIn = true) }
                            _uiEvent.emit(AuthUiEvent.NavToHome)
                        }
                        .onFailure { e ->
                            _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                        }
                }
            }

            is CheckVerificationStatus -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    val isVerified = repository.reloadUser()
                    if (isVerified) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isEmailVerified = true,
                                isLoggedIn = true,
                                isWaitingForVerification = false
                            )
                        }
                        _uiEvent.emit(AuthUiEvent.NavToHome)
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Email not verified yet. Please check your inbox."
                            )
                        }
                    }
                }
            }

            is ResendVerificationEmail -> {
                viewModelScope.launch {
                    repository.resendVerificationEmail()
                        .onSuccess {
                            _state.update { it.copy(errorMessage = "Verification email sent!") }
                        }
                        .onFailure { e ->
                            _state.update { it.copy(errorMessage = e.message) }
                        }
                }
            }

            is LogOut -> {
                repository.logout()
                _state.update { AuthState() }
                viewModelScope.launch {
                    _uiEvent.emit(AuthUiEvent.NavToLogIn)
                }
            }

            is OnEmailChange -> {
                _state.update { currentState ->
                    currentState.copy(email = event.email, errorMessage = null)
                }
            }

            is OnPasswordChange -> {
                _state.update { currentState ->
                    currentState.copy(password = event.password, errorMessage = null)
                }
            }
        }
    }

    private fun performAuthAction(action: suspend () -> Result<FirebaseUser>) {
        if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Email and Password cannot be empty") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            action().onSuccess { user ->
                val needsVerification = !user.isEmailVerified &&
                        user.providerData.any { it.providerId == "password" }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        isLoggedIn = user.isEmailVerified || !needsVerification,
                        isWaitingForVerification = needsVerification
                    )
                }
                if (!needsVerification) {
                    _uiEvent.emit(AuthUiEvent.NavToHome)
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed"
                    )
                }
            }
        }
    }

    private fun checkUserStatus() {
        if (repository.isUserLoggedIn()) {
            _state.update { it.copy(isLoggedIn = true) }
            viewModelScope.launch { _uiEvent.emit(AuthUiEvent.NavToHome) }
        }
    }

    private fun validateInput(): String? {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!_state.value.email.matches(emailPattern.toRegex())) {
            return "Please enter a valid email address."
        }

        val pass = _state.value.password
        if (pass.length < 8) {
            return "Password must be at least 8 characters long."
        }
        if (!pass.any { it.isDigit() }) {
            return "Password must contain at least one number."
        }
        if (!pass.any { it.isUpperCase() }) {
            return "Password must contain at least one uppercase letter."
        }

        return null
    }
}
