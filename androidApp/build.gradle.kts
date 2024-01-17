plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "io.silv.tracker.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "io.silv.tracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(project(":core-ui"))

    implementation(projects.shared)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ui.text)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.foundation)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material.icons.extended.android)

    implementation(libs.supabase.compose.auth)
    implementation(libs.supabase.gotrue.kt)
    implementation(libs.supabase.auth.ui)
    implementation(libs.supabase.postgres)
    implementation(libs.supabase.storage)

    implementation(libs.voyager.koin)
    implementation(libs.voyager.screenModel)
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.tabNavigator)
    implementation(libs.voyager.transitions)

    implementation(libs.unifile)

    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.koin.androidx.workmanager)
    testImplementation(libs.koin.test)
}