plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.plugin)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.xvantage.rental"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xvantage.rental"
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


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/gradle/incremental.annotation.processors"
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
        buildConfig = true
    }
    dataBinding {
        enable = true
    }
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.tools.core)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    implementation(libs.jwtdecode)
    implementation(libs.java.jwt)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)

    implementation(libs.glide)
    implementation(libs.overlap.avatar)
    // Hilt dependencies - Remove duplicates
    implementation(libs.dagger.hilt)

    // Keep only one kapt line and add the AndroidX Hilt compiler
    kapt(libs.hilt.compiler)
//    kapt(libs.hilt.compiler.androidx) // Add this line

    // Keep the ViewModel integration
//    implementation(libs.hilt.lifecycle.viewmodel)


    // If you're using Hilt for ViewModel injection, you can also add:
//    implementation(libs.hilt.lifecycle.viewmodel)
//    kapt(libs.hilt.compiler.androidx)

    implementation(libs.lifecycle.viewmodel.ktx)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

// Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

// WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
