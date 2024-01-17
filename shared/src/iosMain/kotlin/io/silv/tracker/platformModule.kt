package io.silv.tracker

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.toFlowSettings
import io.silv.tracker.data.DriverFactory
import org.koin.dsl.module

actual val platformModule = module {
  single { DriverFactory() }
}

actual val settingsFactory: Settings.Factory = NSUserDefaultsSettings.Factory()

@OptIn(ExperimentalSettingsApi::class)
actual val flowSettings = NSUserDefaultsSettings.Factory().create("app_prefs").toFlowSettings()