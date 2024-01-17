package io.silv.tracker.data.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent

class AndroidUploadWorker(
    workerParameters: WorkerParameters,
    applicationContext: Context
): CoroutineWorker(applicationContext, workerParameters), KoinComponent {


    override suspend fun doWork(): Result {
        return Result.success()
    }

    companion object {

    }
}