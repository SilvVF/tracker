package io.silv.tracker.data.upload

import Upload
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.silv.tracker.data.logs.GetLog
import io.silv.tracker.data.logs.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UploadStore(
    settingsFactory: Settings.Factory,
    private val getLog: GetLog,
) {

    private val store = settingsFactory.create("upload_store")

    /**
     * Counter used to keep the queue order.
     */
    private var counter = 0

    /**
     * Adds a list of uploads to the store.
     *
     * @param uploads the list of uploads to add.
     */
    fun addAll(uploads: List<Upload>) {
        with(store) {
            uploads.forEach { upload ->
                this[getKey(upload)] = serialize(upload)
            }
        }
    }

    /**
     * Removes a upload from the store.
     *
     * @param upload the upload to remove.
     */
    fun remove(upload: Upload) {
        store.remove(getKey(upload))
    }

    /**
     * Removes a list of uploads from the store.
     *
     * @param uploads the uploads to remove.
     */
    fun removeAll(uploads: List<Upload>) {
        with(store) {
            uploads.forEach { upload ->
                remove(getKey(upload))
            }
        }
    }

    /**
     * Removes all the uploads from the store.
     */
    fun clear() {
        store.clear()
    }

    /**
     * Returns the preference's key for the given upload.
     *
     * @param upload the upload.
     */
    private fun getKey(upload: Upload): String {
        return upload.log.id.toString()
    }

    /**
     * Returns the list of downloads to restore. It should be called in a background thread.
     */
    suspend fun restore(): List<Upload> {
        val objs =
            store.keys.map { runCatching { store.get<String>(it) }.getOrNull() }
                .filterNotNull()
                .mapNotNull { deserialize(it) }
                .sortedBy { it.order }

        val uploads = buildList {
            for (obj in objs) {

                val log = getLog.await(obj.logId) ?: continue

                add(Upload(log))
            }

        }
        // Clear the store, downloads will be added again immediately.
        clear()
        return uploads
    }


    /**
     * Converts an upload to a string.
     *
     * @param upload the upload to serialize.
     */
    private fun serialize(upload: Upload): String {
        val obj = UploadObject(upload.log.id, upload.log.createdBy, counter++)
        return try {
            Json.encodeToString<UploadObject>(obj)
        } catch (e: SerializationException) {
             e.stackTraceToString()
        }
    }

    /**
     * Restore a download from a string.
     *
     * @param string the download as string.
     */
    private fun deserialize(string: String): UploadObject? {
        return try {
            Json.decodeFromString<UploadObject>(string)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Serializable
private data class UploadObject(val logId: Long, val createdBy: String?, val order: Int)