package io.silv.tracker

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import io.silv.tracker.data.DriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.get
import org.koin.dsl.module

actual val platformModule = module {
    single { DriverFactory(androidContext()) }
}

actual val settingsFactory: Settings.Factory = runInKoinComponent {
    SharedPreferencesSettings.Factory(get<Context>())
}


private val Context.datastore by preferencesDataStore("io.silv.tracker_preferences")

@OptIn(ExperimentalSettingsImplementation::class, ExperimentalSettingsApi::class)
actual val flowSettings: FlowSettings = runInKoinComponent {

    DataStoreSettings(get<Context>().datastore)
}