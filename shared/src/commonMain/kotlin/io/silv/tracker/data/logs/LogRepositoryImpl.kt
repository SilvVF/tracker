package io.silv.tracker.data.logs

import io.silv.Database
import io.silv.tracker.data.DatabaseHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

class LogRepositoryImpl(
    private val databaseHandler: DatabaseHandler
) {

    suspend fun insert(
        logId: String,
        createdBy: String,
        instant: Instant,
        geoPoint: GeoPoint?,
        synced: Boolean,
    ) {
        databaseHandler.await {
            logsQueries.insert(logId, createdBy, instant, geoPoint?.x, geoPoint?.y, synced)
        }
    }

    fun observeAll(): Flow<List<Log>> {
        return databaseHandler.subscribeToList { logsQueries.selectAll(LogMapper::mapLog) }
    }
}