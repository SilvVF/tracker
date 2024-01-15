plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    id("app.cash.sqldelight")
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("io.silv")
        }
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
            implementation("app.cash.sqldelight:android-driver:2.0.0")

            implementation(libs.koin.android)
        }
        nativeMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.0.0")
        }
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
            //put your multiplatform dependencies here
            implementation("co.touchlab:stately-concurrency:2.0.5")

            implementation("app.cash.sqldelight:coroutines-extensions:2.0.0")

            implementation(libs.koin.core)
            implementation(libs.koin.test)
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