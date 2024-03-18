import org.jetbrains.kotlin.gradle.plugin.extraProperties

buildscript {
    val kotlinVersion: String by project
    val hiltVersion: String by project
    System.setProperty("kotlinVersion", kotlinVersion)
    System.setProperty("hiltVersion", hiltVersion)

    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version System.getProperty("kotlinVersion") apply false
    id("com.google.devtools.ksp") version "${System.getProperty("kotlinVersion")}-1.0.17" apply false
    id("com.google.dagger.hilt.android") version System.getProperty("hiltVersion") apply false

}