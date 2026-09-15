import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.room3)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use(::load)
}
val stadiaApiKey = localProperties.getProperty("STADIA_API_KEY").orEmpty()

val versionProperties = Properties().apply {
    rootProject.file("version.properties")
        .inputStream()
        .use(::load)
}
val appVersionName = requireNotNull(versionProperties.getProperty("VERSION_NAME")) {
    "VERSION_NAME is missing from version.properties"
}.also { versionName ->
    require(versionName.matches(Regex("\\d+\\.\\d+\\.\\d+"))) {
        "VERSION_NAME must use major.minor.patch format"
    }
}
val appVersionCode = requireNotNull(versionProperties.getProperty("VERSION_CODE")) {
    "VERSION_CODE is missing from version.properties"
}.toInt().also { versionCode ->
    require(versionCode > 0) { "VERSION_CODE must be positive" }
}

android {
    namespace = "com.universalwill.sportoss"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.universalwill.sportoss"
        minSdk = 29
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField("String", "STADIA_API_KEY", "\"$stadiaApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    androidResources {
        generateLocaleConfig = true
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.hilt.android)

    ksp(libs.hilt.android.compiler)

    implementation(libs.maplibre.compose)
    implementation(libs.maplibre.compose.material3)
    implementation(libs.maplibre.location)
    runtimeOnly(libs.maplibre.runtime)

    implementation(libs.room3.runtime)
    ksp(libs.room3.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
