@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "town.robin.toadua"
    compileSdk = 33

    defaultConfig {
        applicationId = "town.robin.toadua"
        minSdk = 23
        targetSdk = 33
        versionCode = 6
        versionName = "2.0.0"
        resourceConfigurations.addAll(
            listOf(
                "en",
                "b+qtq+Latn",
                "b+jbo",
                "b+tok",
                "ja",
                "b+zh+Hans",
                "pl",
                "es",
                "tl",
                "fr",
                "de",
                "ru",
                "cs"
            )
        )
        manifestPlaceholders += mapOf(
            "apiScheme" to "https",
            "apiHost" to "toadua.uakci.pl",
            "apiPath" to "/",
        )
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_release"
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // For creation of default methods in interfaces
        freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.material3:material3:1.2.0-alpha02")
    implementation("androidx.compose.animation:animation:1.5.0-beta03")
    implementation("androidx.compose.ui:ui-tooling:1.5.0-beta03")
    implementation("androidx.compose.ui:ui:1.5.0-beta03")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0-beta03")
    implementation("androidx.compose.material:material-icons-extended:1.5.0-beta03")
    implementation("com.google.accompanist:accompanist-flowlayout:0.24.13-rc")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("io.github.z4kn4fein:semver:1.4.2")
}