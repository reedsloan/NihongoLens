plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
//    kotlin("plugin.serialization") version "1.9.23"
}

kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

android {
    namespace = "com.reedsloan.nihongolens"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.reedsloan.nihongolens"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.reedsloan.nihongolens.presentation.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/CONTRIBUTORS.md"
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {
    val roomVersion = "2.6.1"
    val hiltVersion = "2.51"
    val materialVersion = "1.6.4"
    val lifecycleVersion = "2.7.0"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // TEST DEPENDENCIES
    testImplementation("junit:junit:4.13.2")
    // hilt testing
    testImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // UI TEST DEPENDENCIES
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Compose UI Tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room Database
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Kotlin Extensions and Coroutines support for Room Database
    implementation("androidx.room:room-ktx:$roomVersion")

    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Material icons for Compose
    implementation("androidx.compose.material:material-icons-core:$materialVersion")
    implementation("androidx.compose.material:material-icons-extended-android:$materialVersion")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // To recognize Japanese script
    implementation("com.google.mlkit:text-recognition-japanese:16.0.0")

    // CameraX core library using the camera2 implementation
    val cameraxVersion = "1.4.0-alpha04"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    // If you want to additionally use the CameraX VideoCapture library
    implementation("androidx.camera:camera-video:${cameraxVersion}")
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation("androidx.camera:camera-mlkit-vision:${cameraxVersion}")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:${cameraxVersion}")

    // Lifecycle ViewModel Compose (Allows ViewModel to be used in Compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // ConstraintLayout Compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Kotori
//    implementation("com.github.wanasit.kotori:kotori:1.0.0")

    // Google Fonts API
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.3")

    // Moshi
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    implementation("com.squareup.moshi:moshi:1.15.1")

    // Kuromoji
    implementation("com.atilika.kuromoji:kuromoji-ipadic:0.9.0")
}