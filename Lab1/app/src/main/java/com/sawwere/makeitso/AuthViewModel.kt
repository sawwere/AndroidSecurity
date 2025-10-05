package com.sawwere.makeitso

import com.google.firebase.auth.FirebaseUser
import com.sawwere.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {

    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    data class SignInState(
        val currentUser: FirebaseUser? = null,
    )

    fun isAuthenticated(): Boolean {
        println(authRepository.currentUser?.isAnonymous)
        return authRepository.currentUser != null && !authRepository.currentUser!!.isAnonymous
    }
}