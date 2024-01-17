package io.silv.tracker.data.upload

import kotlinx.serialization.Serializable

/**
 * Class used for upload serialization
 *
 * @param logId the id of the log.
 * @param userId the id of the user.
 * @param order the order of the download in the queue.
 */
@Serializable
private data class UploadObject(
    val logId: Long,
    val userId: String,
    val order: Int
)

