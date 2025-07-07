plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
}

android {
    namespace = "com.afin.jauharnafissubmission1expert"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.afin.jauharnafissubmission1expert"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"https://story-api.dicoding.dev/v1/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug { }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    // Core Android
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation (libs.play.services.location)


    // Lifecycle & Architecture Components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Image Loading
    implementation(libs.glide)

    // RecyclerView & CardView
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // SwipeRefreshLayout
    implementation(libs.androidx.swiperefreshlayout)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Image Picker
    implementation("com.github.Dhaval2404:ImagePicker:2.1")

    // Permission Handler
    implementation(libs.dexter)

    // Lottie untuk animasi
    implementation(libs.lottie)
    implementation(libs.androidx.activity)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    ksp(libs.ksp)
}