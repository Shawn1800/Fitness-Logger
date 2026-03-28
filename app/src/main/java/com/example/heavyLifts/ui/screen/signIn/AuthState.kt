package com.example.heavyLifts.ui.screen.signIn

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isSuccess: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isWaitingForVerification: Boolean = false

)

