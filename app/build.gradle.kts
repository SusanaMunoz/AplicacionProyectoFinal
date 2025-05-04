// build.gradle.kts (Módulo)
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tdc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tdc"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    // Firebase (usando BOM para versiones consistentes)
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Material Design
    implementation("com.google.android.material:material:1.9.0")

    // WorkManager
    implementation("androidx.work:work-runtime:2.7.1")

    // Core AndroidX
    implementation("androidx.core:core:1.8.0")

    // Pruebas instrumentadas
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")

    implementation ("com.sun.mail:android-mail:1.6.2")
    implementation ("com.sun.mail:android-activation:1.6.2")

    // JUnit (pruebas locales)
    testImplementation("junit:junit:4.13.2")
}
