plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Plugin necesario para Room.
    // Room lo usa para generar código automáticamente a partir de las anotaciones.
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
}

/*
    Este plugin es el que aparece en el proyecto de mapas.
    Sirve para leer GOOGLE_MAPS_API_KEY desde local.properties
    y usarla en el AndroidManifest.xml.
*/
buildscript {
    dependencies {
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
}