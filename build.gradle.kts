// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript{


    repositories{
        google()
        mavenCentral()
    }

    dependencies{
        classpath("androidx.navigation.safeargs:androidx.navigation.safeargs.gradle.plugin:2.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.47")
    }
}




plugins {
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}