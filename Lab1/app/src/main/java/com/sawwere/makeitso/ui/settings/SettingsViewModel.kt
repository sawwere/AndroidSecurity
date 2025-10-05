package com.sawwere.makeitso.ui.settings

import com.google.firebase.auth.FirebaseUser
import com.sawwere.makeitso.MainViewModel
import com.sawwere.makeitso.data.repository.AuthProvider
import com.sawwere.makeitso.data.repository.AuthRepository
import com.sawwere.makeitso.data.repository.getAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    private val _isAnonymous = MutableStateFlow(true)
    val isAnonymous: StateFlow<Boolean>
        get() = _isAnonymous.asStateFlow()

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    data class SignInUiState(
        val currentUser: FirebaseUser? = null,
        val userEmail: String? = null,
        val authProvider: AuthProvider? = null
    )

    fun loadCurrentUser() {
        launchCatching {
            val currentUser = authRepository.currentUser
            _uiState.value = _uiState.value.copy(
                currentUser = currentUser,
                userEmail = currentUser?.email ?: "Нет данных об электронной почте",
                authProvider = getAuthProvider(currentUser)
            )
            _isAnonymous.value = currentUser != null && currentUser.isAnonymous
        }
    }

    fun signOut() {
        launchCatching {
            authRepository.signOut()
            _shouldRestartApp.value = true
        }
    }

    fun deleteAccount() {
        launchCatching {
            authRepository.deleteAccount()
            _shouldRestartApp.value = true
        }
    }
}