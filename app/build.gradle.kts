import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.julianczaja.stations"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.julianczaja.stations"
        minSdk = 23
        targetSdk = 34
        versionCode = 3
        versionName = "0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.named("debug").get()
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.androidx.datastore.preferences)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
}

tasks.register("downloadStationsJson") {
    doLast {
        val assetsDir = file("$projectDir/src/main/assets/")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }

        val outputFile = file("$assetsDir/stations.json")

        val url = URL("https://koleo.pl/api/v2/main/stations")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("X-KOLEO-Version", "1")
        connection.inputStream.use { input ->
            Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        println("Stations JSON saved in assets")
    }
}

tasks.register("downloadStationKeywordsJson") {
    doLast {
        val assetsDir = file("$projectDir/src/main/assets/")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }

        val outputFile = file("$assetsDir/station_keywords.json")

        val url = URL("https://koleo.pl/api/v2/main/station_keywords")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("X-KOLEO-Version", "1")
        connection.inputStream.use { input ->
            Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        println("Station keywords JSON saved in assets")
    }
}

tasks.named("preBuild") {
    dependsOn("downloadStationsJson")
    dependsOn("downloadStationKeywordsJson")
}
