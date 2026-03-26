package com.example.HeavyLifts.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value=auth.currentUser
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user!!

            // Check if user needs to verify email (skip for Google users)
            if (!user.isEmailVerified && user.providerData.any { it.providerId == "password" }) {
                Result.failure(Exception("Please verify your email address before logging in."))
            } else {
                Result.success(user)
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No account found with this email."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Incorrect password. Please try again."))
        } catch (e: Exception) {
            Result.failure(Exception("Login failed. Check your connection."))
        }
    }

    suspend fun signup(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            // AUTOMATICALLY send verification email on signup
            user.sendEmailVerification().await()

            Result.success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("This email is already registered."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reloadUser(): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            user?.reload()?.await()
            _currentUser.value = firebaseAuth.currentUser
            user?.isEmailVerified ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sends a new verification email if the previous one expired or was lost.
     */
    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null && !user.isEmailVerified) {
                user.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("User is null or already verified."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun isUserLoggedIn(): Boolean {
        val user = firebaseAuth.currentUser
        if (user == null) return false

        // If they used Google, they are "Verified" by default.
        // If they used Email/Pass, check the verified flag.
        val isGoogleUser = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        return isGoogleUser || user.isEmailVerified
    }
    fun logout(){
        firebaseAuth.signOut()
    }

}

