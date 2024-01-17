package io.silv.tracker.data.logs

import kotlinx.datetime.Instant


data class Log(
    val id: Long,
    val logId: String,
    val instant: Instant,
    val synced: Boolean,
    val geoPoint: GeoPoint?,
    val createdBy: String?,
)


data class GeoPoint(
    val x: Long,
    val y: Long,
)