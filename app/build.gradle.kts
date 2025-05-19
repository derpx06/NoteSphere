plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.notesphere"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.notesphere"
        minSdk = 24
        targetSdk = 35
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
        compose = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("androidx.navigation:navigation-compose:2.7.5")
    implementation ("io.coil-kt:coil-compose:2.4.0")
    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // Retrofit
    implementation ("androidx.navigation:navigation-compose:2.7.5")
    // ... other dependencies
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // ... other dependencies
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // ... other dependencies
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // For Fragments, not Compose
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7") // For Fragments,
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("androidx.compose.animation:animation:1.5.0")
    //swipe feature
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.36.0")
    //DataStore
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1") // Or the latest stable version
    implementation ("com.google.mlkit:text-recognition:16.0.0")
    implementation ("com.google.mlkit:face-detection:16.1.6")
    implementation ("com.google.dagger:hilt-android:2.44")
    implementation ("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2") // Use the latest version
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("io.coil-kt:coil-compose:2.4.0")
    // Secure Storage
    implementation ("androidx.security:security-crypto:1.1.0-alpha06")
    // Add Accompanist for ModalBottomSheetLayout
    implementation ("com.google.accompanist:accompanist-navigation-material:0.31.2-alpha")
    // Gson (you may not need this line if you are using Retrofit with the Gson converter)
    implementation("com.google.code.gson:gson:2.10.1")
    // Gson (you may not need this line if you are using Retrofit with the Gson converter)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-cio:2.3.9")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.9")
    implementation("io.ktor:ktor-serialization-gson:2.3.9")
    implementation("io.ktor:ktor-client-logging:2.3.9")
    // Navigation Component (for Jetpack Compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // Navigation Component (for Jetpack Compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.benchmark.common)
    // Navigation component (for Fragments)
    val roomVersion = "2.7.1" // Use the latest version
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")//Kotlin extensions and coroutines support
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Room
    implementation("androidx.room:room-runtime:2.5.2")
    // kapt("androidx.room:room-compiler:2.5.2")  // Uncommented and corrected
    implementation("androidx.compose.material:material-icons-extended:<latest>")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}