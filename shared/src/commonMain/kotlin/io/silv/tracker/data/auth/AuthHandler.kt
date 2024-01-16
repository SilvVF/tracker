package io.silv.tracker.data.auth

import io.github.jan.supabase.gotrue.Auth
import io.silv.tracker.suspendRunCatching

class AuthHandler(
    private val auth: Auth
) {

    suspend fun logout(): Boolean {
        return suspendRunCatching { auth.signOut() }.isSuccess
    }
}