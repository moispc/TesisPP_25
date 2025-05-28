plugins {
    alias(libs.plugins.android.application)


}

android {
    namespace = "com.example.food_front"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.food_front"
        minSdk = 21
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
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    //recyclerview
    implementation(libs.recyclerview)    //para imagenes
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    // Agregar CircleImageView para imágenes de perfil circulares
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // No usaremos el SDK de Mercado Pago para evitar problemas de compatibilidad

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    implementation("androidx.fragment:fragment:1.8.3")
    implementation(libs.fragment)
    implementation(libs.volley)

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

