// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

// Configuración de repositorios para todos los proyectos
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // Repositorio específico para MercadoPago
        maven { url = uri("https://artifacts.mercadolibre.com/repository/android-releases") }
    }
}