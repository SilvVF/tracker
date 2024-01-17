package io.silv.tracker.data.network

import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.network.SupabaseApi
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.ktor.http.ContentType
import io.silv.Logs
import io.silv.tracker.data.logs.Log
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseHelper(
    private val auth: Auth,
    private val storage: Storage,
    private val postgrest: Postgrest,
) {
    val bucket = storage.from("images")

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

    suspend fun insertLogForUser(vararg logs: Log) {
        val user = auth.currentUserOrNull() ?: return

        postgrest.from(LOGS_TABLE).insert(logs.map { it.toDbLog() })
    }

    companion object {
        const val LOGS_TABLE = "logs"
    }

    private fun Log.toDbLog(): LogsDto {
        return LogsDto(
            id = logId,
            deleted = false,
            createdBy = createdBy,
            createdAt = instant,
            cordX = geoPoint?.x?.toDouble(),
            cordY = geoPoint?.y?.toDouble()
        )
    }
}


@Serializable
data class LogsDto(
    val id: String,
    val deleted: Boolean,
    @SerialName("created_by")
    val createdBy: String?,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("cord_x")
    val cordX: Double?,
    @SerialName("cord_y")
    val cordY: Double?
)