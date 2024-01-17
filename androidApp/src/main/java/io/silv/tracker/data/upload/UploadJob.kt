@file:OptIn(ExperimentalCoroutinesApi::class)

package io.silv.tracker.data.upload

import Upload
import UploadImage
import com.hippo.unifile.UniFile
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.uploadAsFlow
import io.silv.tracker.data.logs.Log
import io.silv.tracker.data.network.SupabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.concurrent.Volatile
import kotlin.coroutines.cancellation.CancellationException


class UploadJob(
    private val supabase: SupabaseHelper,
    private val store: UploadStore,
    private val storage: Storage,
    private val provider: ImageProvider,
) {

    /**
     * Queue where active downloads are kept.
     */
    private val _queueState = MutableStateFlow<List<Upload>>(emptyList())
    val queueState = _queueState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var uploaderJob: Job? = null

    /**
     * Whether the downloader is paused
     */
    @Volatile
    var isPaused: Boolean = false
    /**
     * Whether the uploader is running.
     */
    val isRunning: Boolean
        get() = uploaderJob?.isActive ?: false

    /**
     * Starts the downloader. It doesn't do anything if it's already running or there isn't anything
     * to download.
     *
     * @return true if the downloader is started, false otherwise.
     */
    fun start(): Boolean {
        if (isRunning || queueState.value.isEmpty()) {
            return false
        }
        val pending = queueState.value.filter { it.status != Upload.State.UPLOADED }
        pending.forEach {
            if (it.status != Upload.State.QUEUE)
                it.status = Upload.State.QUEUE
        }

        isPaused = false

        launchDownloaderJob()

        return pending.isNotEmpty()
    }

    /**
     * Stops the downloader.
     */
    fun stop(reason: String? = null) {
        cancelUploaderJob()
        queueState.value
            .filter { it.status == Upload.State.UPLOADING }
            .forEach { it.status = Upload.State.ERROR }

        if (reason != null) {
            return
        }

        isPaused = false

        //UploadWorker.stop()
    }

    /**
     * Pauses the downloader
     */
    fun pause() {
        cancelUploaderJob()
        queueState.value
            .filter { it.status == Upload.State.UPLOADING }
            .forEach { it.status = Upload.State.QUEUE }
        isPaused = true
    }

    /**
     * Removes everything from the queue.
     */
    fun clearAndCancelJobQueue() = scope.launch {
        cancelUploaderJob()
        clearQueue()
    }

    private fun clearQueue() {
        _queueState.update {
            it.forEach { upload ->
                if (upload.status == Upload.State.UPLOADING || upload.status == Upload.State.QUEUE) {
                    upload.status = Upload.State.NOT_UPLOADED
                }
            }
            store.clear()
            emptyList()
        }
    }

    /**
     * Destroys the downloader subscriptions.
     */
    private fun cancelUploaderJob() {
        uploaderJob?.cancel()
        uploaderJob = null
    }


    /**
     * Prepares the subscriptions to start downloading.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun launchDownloaderJob() {

        if (isRunning) return

        uploaderJob = scope.launch {
            val activeUploadsFlow = queueState.transformLatest { queue ->
                while (true) {

                    val activeUploads = queue
                        // Ignore completed uploads, leave them in the queue
                        .filterNot { it.status.value > Upload.State.UPLOADING.value }
                        .toList()
                        .take(5)

                    emit(activeUploads)

                    if (activeUploads.isEmpty()) break
                    // Suspend until a download enters the ERROR state
                    val activeUploadsErrorFlow =
                        combine(activeUploads.map(Upload::statusFlow)) { states ->
                            states.contains(Upload.State.ERROR)
                        }
                            .filter { it }
                    activeUploadsErrorFlow.first()
                }
            }
                .distinctUntilChanged()

            // Use supervisorScope to cancel child jobs when the uploader job is cancelled
            supervisorScope {
                val uploadJobs = mutableMapOf<Upload, Job>()

                activeUploadsFlow.collectLatest { activeUploads ->

                    val uploadJobsToStop = uploadJobs.filter { it.key !in activeUploads }

                    uploadJobsToStop.forEach { (upload, job) ->
                        job.cancel()
                        uploadJobs.remove(upload)
                    }

                    val uploadsToStart = activeUploads.filter { it !in uploadJobs }
                    uploadsToStart.forEach { upload ->
                        uploadJobs[upload] = launchUploadJob(upload)
                    }
                }
            }
        }
    }


    private fun CoroutineScope.launchUploadJob(upload: Upload) = launch(Dispatchers.IO) {
        try {
            uploadLog(upload)

            // Remove successful download from queue
            if (upload.status == Upload.State.UPLOADED) {
                removeFromQueue(upload)
            }
            if (areAllUploadsFinished()) {
                stop()
            }
        } catch (e: Throwable) {
            if (e is CancellationException)
                throw e
            stop()
        }
    }

    private suspend fun uploadLog(upload: Upload) {
        try {
            supabase.insertLogForUser(upload.log)

            val uploadImages = getUploadImageList(upload.log.logId, upload.log.createdBy!!)

            upload.status = Upload.State.UPLOADING

            // Start downloading images, consider we can have downloaded images already
            // Concurrently do 2 pages at a time
            var anyFailed = false

            uploadImages.asFlow()
                .flatMapMerge(concurrency = 2) { image ->
                    flow<UploadImage> {
                        withContext(Dispatchers.IO) {

                            val successful = uploadImage(image, upload)

                            if (!successful)
                                anyFailed = true
                        }
                        emit(image)
                    }
                        .flowOn(Dispatchers.IO)
                }
                .collect {
                    // Do when page is downloaded.
                }


            if (anyFailed) {
                upload.status = Upload.State.ERROR
                return
            }

        } catch (error: Throwable) {
            if (error is CancellationException) throw error

            upload.status = Upload.State.ERROR
        }
    }


    private suspend fun uploadImage(image: UploadImage, upload: Upload): Boolean {
        // If the image URL is empty, do nothing
        if (image.path == null) {
            return true
        }

        return try {
            val uploaded = storage.from("images")
                .uploadAsFlow(
                    path = upload.log.createdBy!! + image.path!!,
                    file = File(image.path!!),
                )
                .fold(initial = false) { _, status ->
                    when (status) {
                        is UploadStatus.Progress -> {
                            image.invoke(status.totalBytesSend, status.contentLength)
                            false
                        }
                        is UploadStatus.Success -> true
                    }
                }

            if (!uploaded)
                return false

            image.progress = 100
            image.status = UploadImage.State.UPLOADED
            true
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            // Mark this page as error and allow to download the remaining
            image.progress = 0
            image.status = UploadImage.State.ERROR
            false
        }
    }


    private fun getUploadImageList(logId: String, userId: String): List<UploadImage> {

        val imageDir = provider.getLogsDir(logId, userId)
        val imageFiles = imageDir.listFiles()

        return imageFiles!!.mapIndexed { index, uniFile ->
            UploadImage(
                index,
                uniFile.filePath
            )
        }
    }

    /**
     * Creates a upload object for every log and adds them to the uploads queue.
     *
     * @param logs the logs to upload.
     * @param autoStart whether to start the uploader after enqueuing the logs.
     */
    suspend fun queueLogs(logs: List<Log>, autoStart: Boolean) {

        if (logs.isEmpty()) return

        val logsToQueue = logs.asSequence()
            // Add chapters to queue from the start.
            // Filter out those already enqueued.
            .filter { log -> queueState.value.none { it.log.id == log.id } }
            // Create a download for each one.
            .map { Upload(log = it) }
            .toList()


        if (logsToQueue.isNotEmpty()) {
            addAllToQueue(logsToQueue)

            //UploadWorker.start()
        }
    }

    private fun addAllToQueue(uploads: List<Upload>) {
        _queueState.update {
            uploads.forEach { upload ->
                upload.status = Upload.State.QUEUE
            }
            store.addAll(uploads)
            it + uploads
        }
    }

    /**
     * Returns true if all the queued uploads are in DOWNLOADED or ERROR state.
     */
    private fun areAllUploadsFinished(): Boolean {
        return queueState.value.none { it.status.value <= Upload.State.UPLOADING.value }
    }

    private fun removeFromQueue(upload: Upload) {
        _queueState.update {
            store.remove(upload)
            if (upload.status == Upload.State.UPLOADING || upload.status == Upload.State.QUEUE) {
                upload.status = Upload.State.NOT_UPLOADED
            }
            it - upload
        }
    }
}