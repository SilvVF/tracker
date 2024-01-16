package io.silv.tracker

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.silv.Database
import io.silv.Logs
import io.silv.tracker.data.DatabaseHandler
import io.silv.tracker.data.DatabaseHandlerImpl
import io.silv.tracker.data.DriverFactory
import io.silv.tracker.data.logs.GeoPoint
import io.silv.tracker.data.logs.LogRepositoryImpl
import io.silv.tracker.data.network.SupabaseHelper
import io.silv.tracker.presentation.AuthScreenModel
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {

  single<SqlDriver> { get<DriverFactory>().createDriver() }

  single {
    createSupabaseClient(BuildKonfig.SUPABASE_URL, BuildKonfig.SUPABASE_API_KEY) {
      install(Auth)
      install(Postgrest)
      install(ComposeAuth) {
        googleNativeLogin(serverClientId = BuildKonfig.GOOGLE_WEB_CLIENT_ID)
        appleNativeLogin()
      }

      defaultSerializer = KotlinXSerializer(
        Json {
          prettyPrint = true
          ignoreUnknownKeys = true
          isLenient = true
        }
      )
    }
  }

  single {
    get<SupabaseClient>().auth
  }

  single {
    get<SupabaseClient>().composeAuth
  }

  single {
    get<SupabaseClient>().postgrest
  }

  single {
    Database(
      get(),
      logsAdapter = Logs.Adapter(
        instantAdapter = object : ColumnAdapter<Instant, Long> {
          override fun decode(databaseValue: Long): Instant {
            return Instant.fromEpochMilliseconds(databaseValue)
          }
          override fun encode(value: Instant): Long {
            return value.toEpochMilliseconds()
          }
        },
      )
    )
  }

  single<DatabaseHandler> {
    DatabaseHandlerImpl(get(), get())
  }

  singleOf(::LogRepositoryImpl)

  factoryOf(::AuthScreenModel)
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