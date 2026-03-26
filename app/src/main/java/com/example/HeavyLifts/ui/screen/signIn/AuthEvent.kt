package com.example.HeavyLifts.ui.screen.signIn

sealed interface AuthEvent {
    data object LogIn : AuthEvent
    data object SignUp : AuthEvent
    data object LogOut : AuthEvent
    data class OnEmailChange(val email: String) : AuthEvent
    data class OnPasswordChange(val password: String) : AuthEvent
    data class GoogleSignIn(val idToken: String) : AuthEvent
    data object CheckVerificationStatus : AuthEvent
    data object ResendVerificationEmail : AuthEvent
}
