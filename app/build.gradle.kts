    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.google.services)
        alias(libs.plugins.secrets)
    }

    android {
        namespace = "com.proyecto.red_pro"
        compileSdk = libs.versions.compileSdk.get().toInt()

        defaultConfig {
            applicationId = "com.proyecto.red_pro"
            minSdk = libs.versions.minSdk.get().toInt()
            targetSdk = libs.versions.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            // === Inyección de claves desde secrets.properties / secrets.defaults.properties ===
            val mapsKey = providers.gradleProperty("MAPS_API_KEY").orNull ?: ""
            val firebaseWeb = providers.gradleProperty("FIREBASE_WEB_API_KEY").orNull ?: ""

            // Para usar en código Kotlin (BuildConfig.MAPS_API_KEY...)
            buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")
            buildConfigField("String", "FIREBASE_WEB_API_KEY", "\"$firebaseWeb\"")

            //Para que el placeholder ${MAPS_API_KEY} del AndroidManifest tenga valor
            manifestPlaceholders["MAPS_API_KEY"] = mapsKey
        }

        buildFeatures {
            viewBinding = true
            buildConfig = true
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        kotlinOptions { jvmTarget = "11" }
    }

    dependencies {
        // Firebase BOM: NO pongas versiones en los artefactos ktx
        implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")
        implementation("com.google.firebase:firebase-storage")

        // Coroutines (necesario por callbackFlow/awaitClose)
        implementation(libs.kotlinx.coroutines.android)

        // AndroidX base
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.recyclerview)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.fragment)
        implementation(libs.androidx.constraintlayout)

        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.espresso.core)
    }