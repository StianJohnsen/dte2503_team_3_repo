plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.dashcarr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dashcarr"
        minSdk = 29
        targetSdk = 34
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

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lintOptions {
        disable("ContentDescription")
    }
}

configurations.all {
    resolutionStrategy {
        force("com.j256.ormlite:ormlite-android:6.1")
        force("com.j256.ormlite:ormlite-core:6.1")

    }
}

kapt {
    correctErrorTypes = true
}

dependencies {


    implementation("androidx.cardview:cardview:1.0.0")

    //room
    implementation("androidx.room:room-runtime:2.6.0-alpha03")
    //annotationProcessor("androidx.room:room-compiler:2.5.2") // use kapt for Kotlin
    kapt("androidx.room:room-compiler:2.6.0-alpha03") // use kapt for Kotlin
    implementation("androidx.room:room-ktx:2.6.0-alpha03")

    //JSON Converter
    implementation("com.google.code.gson:gson:2.9.0")

    implementation("androidx.fragment:fragment-ktx:1.6.1")

    implementation("com.facebook.android:facebook-android-sdk:[8,9)")
    implementation("com.facebook.android:facebook-login:16.2.0")

    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    implementation("androidx.multidex:multidex:2.0.1")
    // Hilt compiler
    //Hilt (including Dagger)
    implementation("com.google.dagger:hilt-android:2.47")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    kapt("com.google.dagger:hilt-compiler:2.47")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    //noinspection GradleDependency
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Navigation
    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    //noinspection GradleDependency
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    //Livedata
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    //Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    //OSMdroid
    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("org.osmdroid:osmdroid-wms:6.1.16")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.16")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.16") {
        exclude("com.j256.ormlite", "ormlite-android")
        exclude("com.j256.ormlite", "ormlite-core")
    }

    // Requests
    implementation("com.android.volley:volley:1.2.1")

    // Visualization
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //noinspection GradleDependency
    implementation("androidx.camera:camera-core:1.3.0-alpha02")
    //noinspection GradleDependency
    implementation("androidx.camera:camera-camera2:1.3.0-alpha02")
    //noinspection GradleDependency
    implementation("androidx.camera:camera-lifecycle:1.3.0-alpha02")
    //noinspection GradleDependency
    implementation("androidx.camera:camera-video:1.3.0-alpha02")
    //noinspection GradleDependency
    implementation("androidx.camera:camera-view:1.3.0-alpha02")
    //noinspection GradleDependency
    implementation("androidx.camera:camera-extensions:1.3.0-alpha02")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.biometric:biometric:1.1.0")
}