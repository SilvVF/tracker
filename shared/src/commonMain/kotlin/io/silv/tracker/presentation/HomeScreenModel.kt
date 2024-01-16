package io.silv.tracker.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.silv.tracker.data.auth.AuthHandler
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val auth: Auth,
    private val authHandler: AuthHandler
): StateScreenModel<HomeState>(HomeState(auth.sessionStatus.value)) {

    fun logout() {
        screenModelScope.launch {
            authHandler.logout()
        }
    }
}

data class HomeState(
    val status: SessionStatus
)