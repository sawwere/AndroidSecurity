package com.sawwere.makeitso.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.sawwere.makeitso.data.datasource.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    val currentUser: FirebaseUser? = authRemoteDataSource.currentUser
    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    suspend fun createGuestAccount() {
        authRemoteDataSource.createGuestAccount()
    }

    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    suspend fun signInWithCredential(credential: AuthCredential) {
        authRemoteDataSource.signIn(credential)
    }

    suspend fun signUp(email: String, password: String) {
       authRemoteDataSource.linkAccount(email, password)
    }

    fun signOut() {
        authRemoteDataSource.signOut()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }
}