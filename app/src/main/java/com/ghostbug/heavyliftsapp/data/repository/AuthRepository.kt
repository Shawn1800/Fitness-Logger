package com.ghostbug.heavyliftsapp.data.repository

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Boolean

    suspend fun signUp(email: String, password: String): Boolean

    suspend fun signInWithGoogle(idToken: String,nonce: String?): Boolean

    suspend fun signOut()

    suspend fun deleteAccount()

    fun getCurrentUserId(): String?

    fun getCurrentUserEmail(): String?

    suspend fun sendPasswordResetEmail(email: String): Boolean

}
