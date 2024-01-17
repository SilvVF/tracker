@file:OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsApi::class)

package io.silv.tracker.domain.base

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.contains
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.toBlockingObservableSettings
import com.russhwolf.settings.coroutines.toBlockingSettings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.silv.tracker.domain.base.AndroidPreference.BooleanPrimitive
import io.silv.tracker.domain.base.AndroidPreference.FloatPrimitive
import io.silv.tracker.domain.base.AndroidPreference.IntPrimitive
import io.silv.tracker.domain.base.AndroidPreference.LongPrimitive
import io.silv.tracker.domain.base.AndroidPreference.StringPrimitive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.stateIn

class BasePreferences(
    val settings: PreferenceStore,
) {

    fun incognitoMode() = settings.getBoolean("incognito_mode", false)
}

class CommonPreferenceStore(
    private val flowSettings: FlowSettings
) : PreferenceStore {

    private val blockingSettings = flowSettings.toBlockingObservableSettings(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return StringPrimitive(
            blockingSettings,
            blockingSettings.getStringFlow(key, defaultValue),
            key,
            defaultValue
        )
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return LongPrimitive(blockingSettings, blockingSettings.getLongFlow(key, defaultValue), key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return IntPrimitive(blockingSettings,  blockingSettings.getIntFlow(key, defaultValue), key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return FloatPrimitive(blockingSettings,  blockingSettings.getFloatFlow(key, defaultValue), key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return BooleanPrimitive(blockingSettings,  blockingSettings.getBooleanFlow(key, defaultValue), key, defaultValue)
    }

    override fun getAll(): Map<String, *> {
        val settings = flowSettings.toBlockingSettings()
        return buildMap<String, Any?> { settings.keys.forEach { put(it, settings[it]) } }
    }
}

sealed class AndroidPreference<T>(
    val preferences: ObservableSettings,
    private val observableFlow: Flow<T>,
    private val key: String,
    private val defaultValue: T,
) : Preference<T> {

    abstract fun read(preferences: ObservableSettings, key: String, defaultValue: T): T

    abstract fun write(key: String, value: T): Unit

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return try {
            read(preferences, key, defaultValue)
        } catch (e: ClassCastException) {
            delete()
            defaultValue
        }
    }

    override fun set(value: T) { write(key, value) }

    override fun isSet(): Boolean {
        return preferences.contains(key)
    }

    override fun delete() {
        preferences.remove(key)
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun changes(): Flow<T> {
        return observableFlow.conflate()
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }

    class StringPrimitive(
        preferences: ObservableSettings,
        observableFlow: Flow<String>,
        key: String,
        defaultValue: String,
    ) : AndroidPreference<String>(preferences, observableFlow, key, defaultValue) {
        override fun read(
            preferences: ObservableSettings,
            key: String,
            defaultValue: String,
        ): String {
            return preferences[key, defaultValue]
        }

        override fun write(key: String, value: String) { preferences[key] = value }
    }

    class LongPrimitive(
        preferences: ObservableSettings,
        observableFlow: Flow<Long>,
        key: String,
        defaultValue: Long,
    ) : AndroidPreference<Long>(preferences, observableFlow, key, defaultValue) {
        override fun read(preferences: ObservableSettings, key: String, defaultValue: Long): Long {
            return preferences[key, defaultValue]
        }

        override fun write(key: String, value: Long) { preferences[key] = value }
    }

    class IntPrimitive(
        preferences: ObservableSettings,
        observableFlow: Flow<Int>,
        key: String,
        defaultValue: Int,
    ) : AndroidPreference<Int>(preferences, observableFlow, key, defaultValue) {
        override fun read(preferences: ObservableSettings, key: String, defaultValue: Int): Int {
            return preferences[key, defaultValue]
        }

        override fun write(key: String, value: Int) { preferences[key] = value }
    }

    class FloatPrimitive(
        preferences: ObservableSettings,
        observableFlow: Flow<Float>,
        key: String,
        defaultValue: Float,
    ) : AndroidPreference<Float>(preferences, observableFlow, key, defaultValue) {
        override fun read(preferences: ObservableSettings, key: String, defaultValue: Float): Float {
            return preferences[key, defaultValue]
        }

        override fun write(key: String, value: Float) { preferences[key] = value }
    }

    class BooleanPrimitive(
        preferences: ObservableSettings,
        observableFlow: Flow<Boolean>,
        key: String,
        defaultValue: Boolean,
    ) : AndroidPreference<Boolean>(preferences, observableFlow, key, defaultValue) {

        override fun read(
            preferences: ObservableSettings,
            key: String,
            defaultValue: Boolean,
        ): Boolean {
            return preferences[key, defaultValue]
        }

        override fun write(key: String, value: Boolean) { preferences[key] = value }
    }
}

interface PreferenceStore {

    fun getString(key: String, defaultValue: String = ""): Preference<String>

    fun getLong(key: String, defaultValue: Long = 0): Preference<Long>

    fun getInt(key: String, defaultValue: Int = 0): Preference<Int>

    fun getFloat(key: String, defaultValue: Float = 0f): Preference<Float>

    fun getBoolean(key: String, defaultValue: Boolean = false): Preference<Boolean>

//    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Preference<Set<String>>
//
//    fun <T> getObject(
//        key: String,
//        defaultValue: T,
//        serializer: (T) -> String,
//        deserializer: (String) -> T,
//    ): Preference<T>

    fun getAll(): Map<String, *>
}

//inline fun <reified T : Enum<T>> PreferenceStore.getEnum(
//    key: String,
//    defaultValue: T,
//): Preference<T> {
//    return getObject(
//        key = key,
//        defaultValue = defaultValue,
//        serializer = { it.name },
//        deserializer = {
//            try {
//                enumValueOf(it)
//            } catch (e: IllegalArgumentException) {
//                defaultValue
//            }
//        }
//    )
//}

interface Preference<T> {

    fun key(): String

    fun get(): T

    fun set(value: T)

    fun isSet(): Boolean

    fun delete()

    fun defaultValue(): T

    fun changes(): Flow<T>

    fun stateIn(scope: CoroutineScope): StateFlow<T>

    companion object {
        /**
         * A preference that should not be exposed in places like backups without user consent.
         */
        fun isPrivate(key: String): Boolean {
            return key.startsWith(PRIVATE_PREFIX)
        }
        fun privateKey(key: String): String {
            return "${PRIVATE_PREFIX}$key"
        }

        /**
         * A preference used for internal app state that isn't really a user preference
         * and therefore should not be in places like backups.
         */
        fun isAppState(key: String): Boolean {
            return key.startsWith(APP_STATE_PREFIX)
        }
        fun appStateKey(key: String): String {
            return "${APP_STATE_PREFIX}$key"
        }

        private const val APP_STATE_PREFIX = "__APP_STATE_"
        private const val PRIVATE_PREFIX = "__PRIVATE_"
    }
}

inline fun <reified T, R : T> Preference<T>.getAndSet(crossinline block: (T) -> R) = set(
    block(get()),
)

operator fun <T> Preference<Set<T>>.plusAssign(item: T) {
    set(get() + item)
}

operator fun <T> Preference<Set<T>>.minusAssign(item: T) {
    set(get() - item)
}

fun Preference<Boolean>.toggle(): Boolean {
    set(!get())
    return get()
}