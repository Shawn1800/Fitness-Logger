package com.ghostbug.heavyLifts.ui.screen.signUp

sealed class SignUpEvent {
    data class OnEmailChange(val email: String) : SignUpEvent()
    data class OnPasswordChange(val password: String) : SignUpEvent()
    object OnSignUpClick : SignUpEvent()
    object OnNavigateToSignIn : SignUpEvent()

}