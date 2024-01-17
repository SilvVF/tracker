package io.silv.tracker

import io.silv.tracker.data.upload.ImageProvider
import io.silv.tracker.data.upload.UploadWorker
import io.silv.tracker.data.upload.Uploader
import io.silv.tracker.presentation.auth.AuthScreenModel
import io.silv.tracker.presentation.logs.LogsCreateScreenModel
import io.silv.tracker.presentation.logs.LogsViewScreenModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val androidModule = module {

    singleOf(::Uploader)

    singleOf(::ImageProvider)

    workerOf(::UploadWorker)

    factoryOf(::LogsCreateScreenModel)

    factoryOf(::AuthScreenModel)

    factoryOf(::LogsViewScreenModel)
}

