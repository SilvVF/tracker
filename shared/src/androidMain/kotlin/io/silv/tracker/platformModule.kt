package io.silv.tracker

import io.silv.tracker.data.DriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
  single { DriverFactory(androidContext()) }
}