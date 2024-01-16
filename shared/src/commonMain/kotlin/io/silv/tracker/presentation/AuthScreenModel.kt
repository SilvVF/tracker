package io.silv.tracker.presentation

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.silv.tracker.data.network.SupabaseHelper
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class AuthScreenModel(
    private val auth: Auth
): StateScreenModel<AuthState>(AuthState()) {

    init {
        auth.sessionStatus.onEach { status ->
            mutableState.update { state -> state.copy(status = status) }
        }
            .launchIn(screenModelScope)
    }
}

data class AuthState(
    val status: SessionStatus = SessionStatus.NotAuthenticated
)