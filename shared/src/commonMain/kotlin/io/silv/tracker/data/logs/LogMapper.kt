package io.silv.tracker.data.logs

import kotlinx.datetime.Instant

object LogMapper {

    fun mapLog(
        id: Long,
        logId: String,
        createdBy: String,
        createdAt: Instant,
        locationX: Long?,
        locationY: Long?,
        synced: Boolean
    ) = Log(
        id = id,
        logId = logId,
        createdBy = createdBy,
        instant = createdAt,
        geoPoint = if (locationX != null && locationY != null) {
            GeoPoint(
                locationX,
                locationY
            )
        } else null,
        synced = synced
    )
}