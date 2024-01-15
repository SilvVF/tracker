package io.silv.tracker.data

import co.touchlab.stately.concurrency.AtomicInt
import co.touchlab.stately.concurrency.ThreadLocalRef
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

expect fun identityHashCode(instance: Any?): Int

/**
 * Defines elements in [CoroutineContext] that are installed into thread context
 * every time the coroutine with this element in the context is resumed on a thread.
 *
 * Implementations of this interface define a type [S] of the thread-local state that they need to store on
 * resume of a coroutine and restore later on suspend. The infrastructure provides the corresponding storage.
 *
 * Example usage looks like this:
 *
 * ```
 * // Appends "name" of a coroutine to a current thread name when coroutine is executed
 * class CoroutineName(val name: String) : ThreadContextElement<String> {
 *     // declare companion object for a key of this element in coroutine context
 *     companion object Key : CoroutineContext.Key<CoroutineName>
 *
 *     // provide the key of the corresponding context element
 *     override val key: CoroutineContext.Key<CoroutineName>
 *         get() = Key
 *
 *     // this is invoked before coroutine is resumed on current thread
 *     override fun updateThreadContext(context: CoroutineContext): String {
 *         val previousName = Thread.currentThread().name
 *         Thread.currentThread().name = "$previousName # $name"
 *         return previousName
 *     }
 *
 *     // this is invoked after coroutine has suspended on current thread
 *     override fun restoreThreadContext(context: CoroutineContext, oldState: String) {
 *         Thread.currentThread().name = oldState
 *     }
 * }
 *
 * // Usage
 * launch(Dispatchers.Main + CoroutineName("Progress bar coroutine")) { ... }
 * ```
 *
 * Every time this coroutine is resumed on a thread, UI thread name is updated to
 * "UI thread original name # Progress bar coroutine" and the thread name is restored to the original one when
 * this coroutine suspends.
 *
 * To use [ThreadLocal] variable within the coroutine use [ThreadLocal.asContextElement][asContextElement] function.
 *
 * ### Reentrancy and thread-safety
 *
 * Correct implementations of this interface must expect that calls to [restoreThreadContext]
 * may happen in parallel to the subsequent [updateThreadContext] and [restoreThreadContext] operations.
 * See [CopyableThreadContextElement] for advanced interleaving details.
 *
 * All implementations of [ThreadContextElement] should be thread-safe and guard their internal mutable state
 * within an element accordingly.
 */
interface ThreadContextElement<S> : CoroutineContext.Element {
  /**
   * Updates context of the current thread.
   * This function is invoked before the coroutine in the specified [context] is resumed in the current thread
   * when the context of the coroutine this element.
   * The result of this function is the old value of the thread-local state that will be passed to [restoreThreadContext].
   * This method should handle its own exceptions and do not rethrow it. Thrown exceptions will leave coroutine which
   * context is updated in an undefined state and may crash an application.
   *
   * @param context the coroutine context.
   */
  fun updateThreadContext(context: CoroutineContext): S
  /**
   * Restores context of the current thread.
   * This function is invoked after the coroutine in the specified [context] is suspended in the current thread
   * if [updateThreadContext] was previously invoked on resume of this coroutine.
   * The value of [oldState] is the result of the previous invocation of [updateThreadContext] and it should
   * be restored in the thread-local state by this function.
   * This method should handle its own exceptions and do not rethrow it. Thrown exceptions will leave coroutine which
   * context is updated in an undefined state and may crash an application.
   *
   * @param context the coroutine context.
   * @param oldState the value returned by the previous invocation of [updateThreadContext].
   */
  fun restoreThreadContext(context: CoroutineContext, oldState: S)
}

// top-level data class for a nicer out-of-the-box toString representation and class name
internal data class ThreadLocalKey(private val threadLocal: ThreadLocalRef<*>) : CoroutineContext.Key<ThreadLocalElement<*>>

/**
 * Wraps [ThreadLocal] into [ThreadContextElement]. The resulting [ThreadContextElement]
 * maintains the given [value] of the given [ThreadLocal] for coroutine regardless of the actual thread its is resumed on.
 * By default [ThreadLocal.get] is used as a value for the thread-local variable, but it can be overridden with [value] parameter.
 * Beware that context element **does not track** modifications of the thread-local and accessing thread-local from coroutine
 * without the corresponding context element returns **undefined** value. See the examples for a detailed description.
 *
 *
 * Example usage:
 * ```
 * val myThreadLocal = ThreadLocal<String?>()
 * ...
 * println(myThreadLocal.get()) // Prints "null"
 * launch(Dispatchers.Default + myThreadLocal.asContextElement(value = "foo")) {
 *   println(myThreadLocal.get()) // Prints "foo"
 *   withContext(Dispatchers.Main) {
 *     println(myThreadLocal.get()) // Prints "foo", but it's on UI thread
 *   }
 * }
 * println(myThreadLocal.get()) // Prints "null"
 * ```
 *
 * The context element does not track modifications of the thread-local variable, for example:
 *
 * ```
 * myThreadLocal.set("main")
 * withContext(Dispatchers.Main) {
 *   println(myThreadLocal.get()) // Prints "main"
 *   myThreadLocal.set("UI")
 * }
 * println(myThreadLocal.get()) // Prints "main", not "UI"
 * ```
 *
 * Use `withContext` to update the corresponding thread-local variable to a different value, for example:
 * ```
 * withContext(myThreadLocal.asContextElement("foo")) {
 *     println(myThreadLocal.get()) // Prints "foo"
 * }
 * ```
 *
 * Accessing the thread-local without corresponding context element leads to undefined value:
 * ```
 * val tl = ThreadLocal.withInitial { "initial" }
 *
 * runBlocking {
 *   println(tl.get()) // Will print "initial"
 *   // Change context
 *   withContext(tl.asContextElement("modified")) {
 *     println(tl.get()) // Will print "modified"
 *   }
 *   // Context is changed again
 *    println(tl.get()) // <- WARN: can print either "modified" or "initial"
 * }
 * ```
 * to fix this behaviour use `runBlocking(tl.asContextElement())`
 */
fun <T> ThreadLocalRef<T>.asContextElement(value: T = get()!!): ThreadContextElement<T> =
  ThreadLocalElement(value, this)


internal class ThreadLocalElement<T>(
  private val value: T,
  private val threadLocal: ThreadLocalRef<T>
) : ThreadContextElement<T> {
  override val key: CoroutineContext.Key<*> = ThreadLocalKey(threadLocal)

  override fun updateThreadContext(context: CoroutineContext): T {
    val oldState = threadLocal.get()
    threadLocal.set(value)
    // asserting not null throws NullPointerException which is the same
    // behaviour as Java ThreadLocal
    return oldState!!
  }
  override fun restoreThreadContext(context: CoroutineContext, oldState: T) {
    threadLocal.set(oldState)
  }

  // this method is overridden to perform value comparison (==) on key
  override fun minusKey(key: CoroutineContext.Key<*>): CoroutineContext {
    return if (this.key == key) EmptyCoroutineContext else this
  }

  // this method is overridden to perform value comparison (==) on key
  override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E? =
    @Suppress("UNCHECKED_CAST")
    if (this.key == key) this as E else null

  override fun toString(): String = "ThreadLocal(value=$value, threadLocal = $threadLocal)"
}
/**
 * Returns the transaction dispatcher if we are on a transaction, or the database dispatchers.
 */
internal suspend fun DatabaseHandlerImpl.getCurrentDatabaseContext(): CoroutineContext {
  return coroutineContext[TransactionElement]?.transactionDispatcher ?: queryDispatcher
}

/**
 * Calls the specified suspending [block] in a database transaction. The transaction will be
 * marked as successful unless an exception is thrown in the suspending [block] or the coroutine
 * is cancelled.
 *
 * SQLDelight will only perform at most one transaction at a time, additional transactions are queued
 * and executed on a first come, first serve order.
 *
 * Performing blocking database operations is not permitted in a coroutine scope other than the
 * one received by the suspending block. It is recommended that all [Dao] function invoked within
 * the [block] be suspending functions.
 *
 * The dispatcher used to execute the given [block] will utilize threads from SQLDelight's query executor.
 */
internal suspend fun <T> DatabaseHandlerImpl.withTransaction(block: suspend () -> T): T {
  // Use inherited transaction context if available, this allows nested suspending transactions.
  val transactionContext =
    coroutineContext[TransactionElement]?.transactionDispatcher ?: createTransactionContext()
  return withContext(transactionContext) {
    val transactionElement = coroutineContext[TransactionElement]!!
    transactionElement.acquire()
    try {
      db.transactionWithResult {
        runBlocking(transactionContext) {
          block()
        }
      }
    } finally {
      transactionElement.release()
    }
  }
}

/**
 * Creates a [CoroutineContext] for performing database operations within a coroutine transaction.
 *
 * The context is a combination of a dispatcher, a [TransactionElement] and a thread local element.
 *
 * * The dispatcher will dispatch coroutines to a single thread that is taken over from the SQLDelight
 * query executor. If the coroutine context is switched, suspending DAO functions will be able to
 * dispatch to the transaction thread.
 *
 * * The [TransactionElement] serves as an indicator for inherited context, meaning, if there is a
 * switch of context, suspending DAO methods will be able to use the indicator to dispatch the
 * database operation to the transaction thread.
 *
 * * The thread local element serves as a second indicator and marks threads that are used to
 * execute coroutines within the coroutine transaction, more specifically it allows us to identify
 * if a blocking DAO method is invoked within the transaction coroutine. Never assign meaning to
 * this value, for now all we care is if its present or not.
 */
private suspend fun DatabaseHandlerImpl.createTransactionContext(): CoroutineContext {
  val controlJob = Job()
  // make sure to tie the control job to this context to avoid blocking the transaction if
  // context get cancelled before we can even start using this job. Otherwise, the acquired
  // transaction thread will forever wait for the controlJob to be cancelled.
  // see b/148181325
  coroutineContext[Job]?.invokeOnCompletion {
    controlJob.cancel()
  }

  val dispatcher = transactionDispatcher.acquireTransactionThread(controlJob)
  val transactionElement = TransactionElement(controlJob, dispatcher)
  val threadLocalElement =
    suspendingTransactionId.asContextElement(identityHashCode(controlJob))
  return dispatcher + transactionElement + threadLocalElement
}

/**
 * Acquires a thread from the executor and returns a [ContinuationInterceptor] to dispatch
 * coroutines to the acquired thread. The [controlJob] is used to control the release of the
 * thread by cancelling the job.
 */
private suspend fun CoroutineDispatcher.acquireTransactionThread(
  controlJob: Job,
): ContinuationInterceptor {
  return suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
      // We got cancelled while waiting to acquire a thread, we can't stop our attempt to
      // acquire a thread, but we can cancel the controlling job so once it gets acquired it
      // is quickly released.
      controlJob.cancel()
    }
    try {
      dispatch(EmptyCoroutineContext, Runnable {
        runBlocking {
          // Thread acquired, resume coroutine
          continuation.resume(coroutineContext[ContinuationInterceptor]!!)
          controlJob.join()
        }
      })
    } catch (ex: Exception) {
      // Couldn't acquire a thread, cancel coroutine
      continuation.cancel(
        IllegalStateException(
          "Unable to acquire a thread to perform the database transaction",
          ex,
        ),
      )
    }
  }
}

/**
 * A [CoroutineContext.Element] that indicates there is an on-going database transaction.
 */
private class TransactionElement(
  private val transactionThreadControlJob: Job,
  val transactionDispatcher: ContinuationInterceptor,
) : CoroutineContext.Element {

  companion object Key : CoroutineContext.Key<TransactionElement>

  override val key: CoroutineContext.Key<TransactionElement>
    get() = TransactionElement

  /**
   * Number of transactions (including nested ones) started with this element.
   * Call [acquire] to increase the count and [release] to decrease it. If the count reaches zero
   * when [release] is invoked then the transaction job is cancelled and the transaction thread
   * is released.
   */
  private val referenceCount = AtomicInt(0)

  fun acquire() {
    referenceCount.incrementAndGet()
  }

  fun release() {
    val count = referenceCount.decrementAndGet()
    if (count < 0) {
      throw IllegalStateException("Transaction was never started or was already released")
    } else if (count == 0) {
      // Cancel the job that controls the transaction thread, causing it to be released.
      transactionThreadControlJob.cancel()
    }
  }
}