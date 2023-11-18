// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}

buildscript {
    repositories {
        // Make sure that you have the following two repositories
        google()  // Google's Maven repository

        mavenCentral()  // Maven Central repository

    }
    dependencies {

        // Add the dependency for the Google services Gradle plugin
        classpath("com.google.gms:google-services:4.4.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")

    }
}