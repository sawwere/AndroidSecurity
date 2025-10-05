package com.sawwere.makeitso.data.repository

import com.google.firebase.auth.FirebaseUser

enum class AuthProvider(val displayName: String) {
    GOOGLE("Google"),
    EMAIL("Email и пароль"),
    UNKNOWN("Неизвестный провайдер");
}

fun getAuthProvider(user: FirebaseUser?): AuthProvider? {
    if (user == null) return null

    return user.providerData.lastOrNull()?.providerId?.let { providerId ->
        when {
            providerId.contains("google") -> AuthProvider.GOOGLE
            providerId == "password" -> AuthProvider.EMAIL
            else -> AuthProvider.UNKNOWN
        }
    } ?: AuthProvider.UNKNOWN
}