package io.silv.tracker.data

import app.cash.sqldelight.db.SqlDriver
import io.silv.Database

expect class DriverFactory {
  fun createDriver(): SqlDriver
}