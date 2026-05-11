package com.ghostbug.heavyliftsapp.data.repository

import android.util.Log
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.Postgrest

class AuthRepositoryImpl(
    private val auth: Auth,
    val postgrest: Postgrest
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signUp(email: String, password: String): Boolean {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signInWithGoogle(idToken: String, nonce: String?): Boolean {
        return try {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.nonce = nonce
                this.provider = Google
            }
            true
        } catch (e: Exception) {
            Log.e("AUTH", "Google Sign-in Error", e)
            false
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount() {
        postgrest.rpc("delete_my_account")
        auth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUserOrNull()?.email
    }

    override suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.resetPasswordForEmail(email)
            true
        } catch (e: Exception) {
            false
        }
    }
}
