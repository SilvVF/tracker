package io.silv.tracker.data.logs

import kotlinx.datetime.Instant

data class Log(
    val id: Long,
    val logId: String,
    val createdBy: String,
    val instant: Instant,
    val geoPoint: GeoPoint,
    val synced: Boolean,
)


data class GeoPoint(
    val x: Long,
    val y: Long,
)