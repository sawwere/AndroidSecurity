package com.sawwere.makeitso.ui.signin

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.internal.Logger.TAG
import com.sawwere.makeitso.MainViewModel
import com.sawwere.makeitso.data.model.ErrorMessage
import com.sawwere.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseUser
import com.sawwere.makeitso.R
import com.sawwere.makeitso.data.repository.AuthProvider
import kotlinx.coroutines.launch

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : MainViewModel() {

    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    fun signIn(
        email: String,
        password: String,
        showErrorSnackbar: (ErrorMessage) -> Unit
    ) {
        launchCatching(showErrorSnackbar) {
            authRepository.signIn(email, password)
            //checkCurrentUser()
            _shouldRestartApp.value = true
        }
    }

    fun signInWithGoogle(
        context: Context,
        showErrorSnackbar: (ErrorMessage) -> Unit

    ) {
        launchCatching(showErrorSnackbar) {
            val credentialManager = CredentialManager.create(context)
            val signInWithGoogleOption = GetSignInWithGoogleOption
                .Builder(serverClientId = context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            launchCredentialManager(credentialManager, request, context)
            _shouldRestartApp.value = true
        }

    }

    private suspend fun launchCredentialManager(
        credentialManager: CredentialManager,
        request: GetCredentialRequest,
        context: Context
    ) {
        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            createGoogleIdToken(result.credential)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            throw e
        }
    }

    private fun createGoogleIdToken(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        println("enter firebaseAuthWithGoogle ")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        launchCatching {
            authRepository.signInWithCredential(credential)
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = authRepository.currentUser
        }
    }
}