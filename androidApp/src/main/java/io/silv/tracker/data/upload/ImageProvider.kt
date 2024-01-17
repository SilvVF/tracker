package io.silv.tracker.data.upload

import android.content.Context
import android.os.Environment
import com.hippo.unifile.UniFile
import io.silv.tracker.android.R
import io.silv.tracker.data.logs.Log
import java.io.File


class ImageProvider(
    context: Context
) {
    private val storageManager = StorageManager(context)

    private val imageDir: UniFile? = storageManager.getDownloadsDirectory()


    internal fun getLogsDir(logId: String, createdBy: String): UniFile {
        try {
            return imageDir!!
                .createDirectory(getUserDirName(createdBy))!!
                .createDirectory(getLogDirName(logId))!!
        } catch (e: Throwable) {
            android.util.Log.e("DownloadProvider","Invalid download directory")
            throw Exception("Invalid download dir")
        }
    }

    /**
     * Returns the download directory name for a manga.
     *
     * @param logId the title of the manga to query.
     */
    fun getLogDirName(logId: String): String {
        return buildValidFilename(logId)
    }

    /**
     * Returns the download directory name for a manga.
     *
     * @param logId the title of the manga to query.
     */
    fun getUserDirName(userId: String): String {
        return buildValidFilename(userId)
    }
    /**
     * Mutate the given filename to make it valid for a FAT filesystem,
     * replacing any invalid characters with "_". This method doesn't allow hidden files (starting
     * with a dot), but you can manually add it later.
     */
    fun buildValidFilename(origName: String): String {
        val name = origName.trim('.', ' ')
        if (name.isEmpty()) {
            return "(invalid)"
        }
        val sb = StringBuilder(name.length)
        name.forEach { c ->
            if (isValidFatFilenameChar(c)) {
                sb.append(c)
            } else {
                sb.append('_')
            }
        }
        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit minus 15 reserved characters.
        return sb.toString().take(240)
    }

    /**
     * Returns true if the given character is a valid filename character, false otherwise.
     */
    private fun isValidFatFilenameChar(c: Char): Boolean {
        if (0x00.toChar() <= c && c <= 0x1f.toChar()) {
            return false
        }
        return when (c) {
            '"', '*', '/', ':', '<', '>', '?', '\\', '|', 0x7f.toChar() -> false
            else -> true
        }
    }
}

private class StorageManager(
    context: Context,
) {

    private var baseDir: UniFile? = UniFile.fromFile(
        File(context.getExternalFilesDir(null)?.absolutePath + File.separator + context.getString(R.string.app_name))
    )


    fun getDownloadsDirectory(): UniFile? {
        return baseDir?.createDirectory(DOWNLOADS_PATH)
    }
}

private const val DOWNLOADS_PATH = "downloads"