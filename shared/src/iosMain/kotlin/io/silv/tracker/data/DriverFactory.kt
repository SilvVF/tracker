package io.silv.tracker.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import io.silv.tracker.Database

actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(
      Database.Schema,
      "tracker.db",
      onConfiguration = { config: DatabaseConfiguration ->
        config.copy(
          extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true)
        )
      }
    )
  }
}