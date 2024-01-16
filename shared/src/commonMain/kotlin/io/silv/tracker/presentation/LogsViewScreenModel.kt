package io.silv.tracker.presentation

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.silv.tracker.data.auth.AuthHandler
import io.silv.tracker.data.logs.Log
import io.silv.tracker.data.logs.LogsHandler
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class LogsViewScreenModel(
    private val auth: Auth,
    private val authHandler: AuthHandler,
    private val logsHandler: LogsHandler
): StateScreenModel<LogsViewState>(LogsViewState(status = auth.sessionStatus.value)) {

    fun logout() {
        screenModelScope.launch {
            authHandler.logout()
        }
    }

    fun insertLog() {
       screenModelScope.launch {
            logsHandler.insertLog(Clock.System.now(), null, false)
       }
    }
}

data class LogsViewState(
    val status: SessionStatus,
    val logsHistory: List<Log> = emptyList()
)