package com.ghostbug.heavyliftsapp.data.repository

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Boolean
    
    suspend fun signUp(email: String, password: String): Boolean

    suspend fun signInWithGoogle(idToken: String,nonce: String?): Boolean
    
    suspend fun signOut()
    
    fun getCurrentUserId(): String?

    suspend fun sendPasswordResetEmail(email: String): Boolean

}
