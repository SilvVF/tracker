package io.silv.tracker.data.network

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseHelper(
    private val auth: Auth,
    private val postgrest: Postgrest,
) {


    suspend fun getLogsForCurrentUser(): List<LogsDto> {
        val user = auth.currentUserOrNull() ?: return emptyList()

        return postgrest.from(LOGS_TABLE)
            .select {
                filter {
                    eq("created_by", user.id)
                }
            }
            .decodeList<LogsDto>()
    }

    suspend fun insertLogForUser(vararg logs: LogsDto) {
        val user = auth.currentUserOrNull() ?: return

        postgrest.from(LOGS_TABLE).insert(logs.asList())
    }

    companion object {
        const val LOGS_TABLE = "logs"
    }
}


@Serializable
data class LogsDto(
    val id: String,
    val deleted: Boolean,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("cord_x")
    val cordX: Double,
    @SerialName("cord_y")
    val cordY: Double
)