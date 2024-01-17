package io.silv.tracker

import org.koin.core.component.KoinComponent
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (exception: Exception) {
        Result.failure(exception)
    }

inline fun <T> runInKoinComponent(
    crossinline block: KoinComponent.() -> T
): T {
    val koin = object: KoinComponent{}

    return block(koin)
}