package com.example.demo103.ui.screen.signIn

import com.google.android.gms.auth.api.Auth

sealed interface AuthEvent {
    data object  LogIn : AuthEvent
    data object  SignIn : AuthEvent
    data object LogOut: AuthEvent
    data class OnEmailChange(val email:String ): AuthEvent
    data class  OnPasswordChange(val password: String): AuthEvent
}