plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "co.privado.finly"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.privado.finly"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase — token publishable (anon) y URL del proyecto app-finanzas
        // Estos valores son los del dashboard; la secret key del LLM vive solo en Supabase Edge Functions.
        buildConfigField("String", "SUPABASE_URL", "\"https://arierbdlmyhoselqiczo.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_tH66oLm1skYHj8ODSGgchQ_3Nq_m0S8\"")
        // Placeholder para Google Web Client ID — debe completarse al activar OAuth (ver docs/frontend 3.3)
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"1036262123558-lrdsraj7pr0a3jp3iore481suila900m.apps.googleusercontent.com\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // BOMs
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.supabase.bom))

    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)

    // Supabase Kotlin — Auth + PostgREST + Functions (+ realtime/storage si se necesitan luego)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.functions)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)

    // Serialization + Coroutines
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore (cola offline temporal, sesión y preferencias)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // WorkManager (sincronización cola offline)
    implementation(libs.androidx.work.runtime.ktx)

    // Biometric
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.biometric)

    // Credential Manager + GoogleId (Google Sign-In moderno)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Coil (íconos de apps, imágenes)
    implementation(libs.coil.compose)

    // Google Fonts para Compose (Fraunces, Inter, IBM Plex Mono — ver Type.kt)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Ktor OkHttp engine (usado por Supabase-Kotlin)
    implementation(libs.ktor.client.okhttp)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
