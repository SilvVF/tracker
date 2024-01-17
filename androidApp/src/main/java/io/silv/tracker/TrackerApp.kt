package io.silv.tracker

import android.app.Application
import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class TrackerApp: Application() {

  override fun onCreate() {
    super.onCreate()

    initKoinAndroid {
      androidLogger()
      androidContext(this@TrackerApp)
      modules(androidModule)
    }
  }
}