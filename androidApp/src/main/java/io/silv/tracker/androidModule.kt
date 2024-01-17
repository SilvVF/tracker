package io.silv.tracker

import io.silv.tracker.presentation.auth.AuthScreenModel
import io.silv.tracker.presentation.logs.LogsViewScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val androidModule = module {

    factoryOf(::AuthScreenModel)

    factoryOf(::LogsViewScreenModel)
}

