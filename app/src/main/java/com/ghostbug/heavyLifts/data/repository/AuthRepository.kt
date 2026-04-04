package com.ghostbug.heavyLifts.data.repository

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Boolean
    
    suspend fun signUp(email: String, password: String): Boolean
    
    suspend fun signInWithGoogle(): Boolean
    
    suspend fun signOut()
    
    fun getCurrentUserId(): String?
}
