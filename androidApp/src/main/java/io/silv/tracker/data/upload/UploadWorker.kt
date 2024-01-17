package io.silv.tracker.data.upload

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class UploadWorker(
    workerParameters: WorkerParameters,
    applicationContext: Context
): CoroutineWorker(applicationContext, workerParameters), KoinComponent {

    val uploader by inject<Uploader>()

    override suspend fun doWork(): Result {
        return Result.success()
    }

    companion object: KoinComponent {

        const val TAG = "UPLOAD_JOB"

        fun start() {
            WorkManager.getInstance(get<Context>())
                .enqueue(
                    OneTimeWorkRequestBuilder<UploadWorker>()
                        .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .addTag(TAG)
                        .build()
                )
        }

        fun stop() {
            WorkManager.getInstance(get())
                .cancelUniqueWork(TAG)
        }
    }
}