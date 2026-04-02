package com.ghostbug.heavyLifts.ui.screen.signIn

sealed class SignInEvent {
    data class OnEmailChange(val email: String) : SignInEvent()
    data class OnPasswordChange(val password: String) : SignInEvent()
    object OnSignInClick : SignInEvent()
    object OnNavigateToSignUp : SignInEvent()
}