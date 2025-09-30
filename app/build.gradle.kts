    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.kotlin.compose)
        alias(libs.plugins.googleService)
    }

    android {
        namespace = "com.dam2.redpro"
        compileSdk = 36

        defaultConfig {
            applicationId = "com.dam2.redpro"
            minSdk = 24
            targetSdk = 36
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        kotlinOptions {
            jvmTarget = "11"
        }
        buildFeatures {
            viewBinding = true
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.lottie) /*Animaciones*/
        implementation(libs.firebaseAuth) /*Autenticación con Firebase*/
        implementation(libs.firebaseDatabase) /*Base de datos con Firebase*/
        implementation(libs.imagePicker) /*Recortar una imagen*/
        implementation(libs.glide) /*Leer imágenes*/
        implementation(libs.storage)/*Subir archivos multimedia*/
        implementation(libs.authGoogle) /*Iniciar sesión con google*/
        implementation(libs.ccp) /*Seleccionar nuestro código telefónico por país*/
        implementation(libs.circleImage)
        implementation(libs.photoView)
        implementation(libs.maps)
        implementation(libs.places)
        implementation(libs.retrofit)
        implementation(libs.converterGson)
        implementation(libs.okhttp3)
        implementation(libs.browser)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }