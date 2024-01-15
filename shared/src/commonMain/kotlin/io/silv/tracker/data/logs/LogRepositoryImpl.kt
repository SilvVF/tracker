package io.silv.tracker.data.logs

import io.silv.tracker.data.DatabaseHandler
import kotlinx.coroutines.flow.Flow

class LogRepositoryImpl(
  private val databaseHandler: DatabaseHandler
) {

  suspend fun insert() {
    databaseHandler.await(true) {
      repeat(100) {
        logsQueries.insert()
      }
    }
  }

  fun subscribeToAll(): Flow<List<Long>> {
    return databaseHandler.subscribeToList {
      logsQueries.selectAll()
    }
  }
}