plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "wilddad.oppo.asspilot"
    compileSdk = 35

    defaultConfig {
        applicationId = "wilddad.oppo.asspilot"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains:annotations:23.0.0")
        }
        exclude(group = "org.jetbrains", module = "annotations-java5")
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
        viewBinding = true
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // OkHttp (SSE streaming)
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.logging.interceptor)

    // JSON
    implementation(libs.gson)

    // Markwon (Markdown rendering)
    implementation(libs.markwon.core)
    implementation(libs.markwon.html)
    implementation(libs.markwon.image)
    implementation(libs.markwon.syntax.highlight)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // SwipeRefreshLayout
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
