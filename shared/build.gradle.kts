import com.codingfeline.buildkonfig.compiler.FieldSpec
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlinSerialization)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("io.silv")
        }
    }
}

buildkonfig {
    packageName = "io.silv.tracker"
    // objectName = "YourAwesomeConfig"
    // exposeObjectWithName = "YourAwesomePublicConfig"
    // Get the API keys from local.properties
    val properties = Properties().also {
        it.load(project.rootProject.file("local.properties").inputStream())
    }

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "GOOGLE_WEB_CLIENT_ID", properties.getProperty("GOOGLE_WEB_CLIENT_ID"))
        buildConfigField(FieldSpec.Type.STRING, "SUPABASE_URL", properties.getProperty("SUPABASE_URL"))
        buildConfigField(FieldSpec.Type.STRING, "SUPABASE_API_KEY", properties.getProperty("SUPABASE_API_KEY"))
    }
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)

            implementation(libs.koin.android)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.work.multiprocess)

            implementation(libs.multiplatform.settings.datastore)
            implementation(libs.androidx.datastore)
        }
        nativeMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.bytestring)

            implementation(libs.stately.concurrency)

            implementation(libs.sqldelight.extension.coroutines)
            implementation(libs.sqldelight.extension.primitive.adapters)

            implementation(libs.voyager.koin)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)

            implementation(libs.supabase.gotrue.kt)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.postgres)
            implementation(libs.supabase.compose.auth)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(libs.koin.core)
            implementation(libs.koin.test)

            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.core)

            implementation(libs.uuid)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "io.silv.tracker"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}