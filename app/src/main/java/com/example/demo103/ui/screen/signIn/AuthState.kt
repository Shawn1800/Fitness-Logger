package com.example.demo103.ui.screen.signIn

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isSuccess: Boolean = false
)

