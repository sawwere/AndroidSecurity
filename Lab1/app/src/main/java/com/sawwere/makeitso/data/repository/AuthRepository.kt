package com.sawwere.makeitso.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.sawwere.makeitso.data.datasource.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {
    init {
        println("AuthRepository instance created: $this")
    }

    val currentUser: FirebaseUser? get() = authRemoteDataSource.currentUser
    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    suspend fun createGuestAccount() {
        authRemoteDataSource.createGuestAccount()
    }

    suspend fun signIn(email: String, password: String) {
        if (currentUser?.isAnonymous == true) {
            deleteAccount()
        }
        authRemoteDataSource.signIn(email, password)
    }

    suspend fun signInWithCredential(credential: AuthCredential) {
        if (currentUser?.isAnonymous == true) {
            deleteAccount()
        }
        authRemoteDataSource.signIn(credential)
    }

    suspend fun signUp(email: String, password: String) {
        if (currentUser?.isAnonymous == true) {
            deleteAccount()
        }
        if (currentUser == null) {
            createGuestAccount()
        }
       authRemoteDataSource.linkAccount(email, password)
    }

    suspend fun signOut() {
//        if (currentUser?.isAnonymous == true) {
//            deleteAccount()
//        }
        authRemoteDataSource.signOut()
    }

    suspend fun deleteAccount() {
        authRemoteDataSource.deleteAccount()
    }

}