package io.silv.tracker

import android.content.Context
import android.os.Environment
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.silv.tracker.data.DriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

actual val platformModule = module {
  single { DriverFactory(androidContext()) }
}

actual val settingsFactory: Settings.Factory = run {

  val koin = object : KoinComponent{}

  return@run SharedPreferencesSettings.Factory(koin.get<Context>())
}
