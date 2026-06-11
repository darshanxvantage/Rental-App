    // Top-level build file where you can add configuration options common to all sub-projects/modules.
    plugins {
        alias(libs.plugins.com.android.application) apply false
        alias(libs.plugins.org.jetbrains.kotlin.android) apply false
        alias(libs.plugins.org.jetbrains.kotlin.kapt) apply false
        alias(libs.plugins.dagger.hilt.plugin) apply false
        id("com.google.gms.google-services") version "4.4.1" apply false

    }

    task("clean", type = Delete::class) {
        delete(rootProject.buildDir)
    }
