package com.example.demo103.ui.screen.signIn

sealed interface  AuthUiEvent {
     data object NavToSignIn : AuthUiEvent
     data object NavToHome: AuthUiEvent
}