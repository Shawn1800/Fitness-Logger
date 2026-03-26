package com.example.demo103.ui.screen.signIn

sealed interface  AuthUiEvent {
     data object NavToSignUp : AuthUiEvent
     data object NavToLogIn : AuthUiEvent
     data object NavToHome: AuthUiEvent
}