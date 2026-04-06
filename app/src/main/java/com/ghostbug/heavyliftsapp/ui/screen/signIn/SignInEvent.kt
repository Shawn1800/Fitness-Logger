package com.ghostbug.heavyliftsapp.ui.screen.signIn

sealed class SignInEvent {
    data class OnEmailChange(val email: String) : SignInEvent()
    data class OnPasswordChange(val password: String) : SignInEvent()
    object OnSignInClick : SignInEvent()
    object OnNavigateToSignUp : SignInEvent()
    object OnForgotPasswordClick : SignInEvent()

    data class OnGoogleSignInResult(val idToken: String, val rawNonce: String) : SignInEvent()

}