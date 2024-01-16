package io.silv.tracker.data.logs

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import io.github.jan.supabase.gotrue.Auth
import kotlinx.datetime.Instant
import kotlin.math.log

class LogsHandler(
    private val auth: Auth,
    private val logsRepository: LogRepositoryImpl
) {

    suspend fun insertLog(
        time: Instant,
        geoPoint: GeoPoint?,
        offline: Boolean,
    ) {
        if (offline) {
            logsRepository.insert(
                uuid4().toString(),
                createdBy = LogRepositoryImpl.offline_user_name,
                instant = time,
                geoPoint = geoPoint,
                synced = false
            )
        } else {

            val user = auth.currentUserOrNull() ?: return

            logsRepository.insert(
                uuid4().toString(),
                createdBy = user.id,
                instant = time,
                geoPoint = geoPoint,
                synced = false
            )
        }
    }
}