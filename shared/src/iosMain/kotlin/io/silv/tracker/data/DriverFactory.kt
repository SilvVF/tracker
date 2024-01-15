package io.silv.tracker.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.silv.Database

actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(Database.Schema, "tracker.db")
  }
}