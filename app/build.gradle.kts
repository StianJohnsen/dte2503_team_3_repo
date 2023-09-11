plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
}

android {
    namespace = "com.example.dashcarr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dashcarr"
        minSdk = 30
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

    buildFeatures{
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

configurations.all{
    resolutionStrategy{
        force("com.j256.ormlite:ormlite-android:6.1")
        force("com.j256.ormlite:ormlite-core:6.1")

    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    //Livedata
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    //Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    //OSMdroid

    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("org.osmdroid:osmdroid-wms:6.1.16")
    implementation ("org.osmdroid:osmdroid-mapsforge:6.1.16")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.16"){
        exclude("com.j256.ormlite","ormlite-android")
        exclude("com.j256.ormlite","ormlite-core")

    }
    //implementation ("org.osmdroid:osmdroid-geopackage:6.1.16")


    /*
     */



}