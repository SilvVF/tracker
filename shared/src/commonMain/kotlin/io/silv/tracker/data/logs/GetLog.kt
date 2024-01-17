package io.silv.tracker.data.logs

import kotlinx.coroutines.flow.Flow
import kotlin.math.log

class GetLog(
    private val logsRepository: LogRepositoryImpl
) {

    suspend fun await(id: Long): Log? {
        return logsRepository.getLogById(id)
    }

    fun subscribe(id: Long): Flow<Log> {
        return logsRepository.observeLogById(id)
    }
}