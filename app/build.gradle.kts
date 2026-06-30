plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kaze.browser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kaze.browser"
        minSdk = 26          // adaptive launcher icon only (no legacy PNG icons); covers ~98% of devices
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // We ship no instrumentation tests; only local JVM unit tests for pure logic.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // R8 full-mode: shrink + obfuscate + resource shrinking for the smallest APK.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            // assembleDebug (CI target) stays unminified for fast, debuggable builds.
            isMinifyEnabled = false
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
        // No BuildConfig / viewBinding / dataBinding — nothing here needs them.
        buildConfig = false
    }
    packaging {
        resources {
            // Drop files we never read; keeps the APK small.
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
        }
    }
}

dependencies {
    // Core Android KTX extensions (Bundle/Context helpers). Tiny, foundational.
    implementation(libs.androidx.core.ktx)
    // Lifecycle-aware coroutine scopes + collectAsStateWithLifecycle for state.
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // viewModelScope for DB writes
    // viewModel() in Compose, holds tab WebViews across recomposition.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // ComponentActivity + setContent entry point.
    implementation(libs.androidx.activity.compose)

    // Compose UI via BOM so all artifacts share one tested version set.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation) // BasicTextField, Canvas, gestures
    implementation(libs.androidx.compose.material3)   // Material 3 surfaces, ripple, typography

    // Room: local SQLite for history + downloads. Nothing cloud-synced.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)            // suspend DAOs + Flow
    ksp(libs.androidx.room.compiler)

    // Local unit test for the ad-block matcher and URL/search resolver (pure logic).
    testImplementation(libs.junit)
}
