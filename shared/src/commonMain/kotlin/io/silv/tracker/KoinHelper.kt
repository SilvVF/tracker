package io.silv.tracker

import app.cash.sqldelight.db.SqlDriver
import io.silv.Database
import io.silv.tracker.data.DatabaseHandler
import io.silv.tracker.data.DatabaseHandlerImpl
import io.silv.tracker.data.DriverFactory
import io.silv.tracker.data.logs.LogRepositoryImpl
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
  single<SqlDriver> { get<DriverFactory>().createDriver() }
  single { Database(get()) }

  single<DatabaseHandler> { DatabaseHandlerImpl(get(), get()) }

  singleOf(::LogRepositoryImpl)
}

expect val platformModule: Module

internal fun getBaseModules() = appModule + platformModule


// in src/commonMain/kotlin
fun initKoinAndroid(additionalModules: List<Module>) {
  startKoin {
    modules(additionalModules + getBaseModules())
  }
}

fun initKoiniOS() {
  startKoin {
    modules(appModule + platformModule)
  }
}