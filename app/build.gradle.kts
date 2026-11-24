import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Supabase credentials are read from local.properties or the root .env (both gitignored),
// so no keys are ever committed to source control.
val secrets = Properties().apply {
    listOf(rootProject.file(".env"), rootProject.file("local.properties"))
        .filter { it.exists() }
        .forEach { f -> f.reader().use { load(it) } }
}
fun secret(name: String, vararg fallbacks: String): String =
    (listOf(name) + fallbacks).firstNotNullOfOrNull { secrets.getProperty(it)?.trim()?.takeIf(String::isNotEmpty) } ?: ""

android {
    namespace = "com.stocktics"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.stocktics"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${secret("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${secret("SUPABASE_PUBLISHABLE_KEY", "SUPABASE_KEY")}\"")
        buildConfigField("String", "SUPABASE_SECRET_KEY", "\"${secret("SUPABASE_SECRET_KEY", "SUPABASE_SERVICE_ROLE_KEY")}\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    testImplementation("org.json:json:20240303")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}