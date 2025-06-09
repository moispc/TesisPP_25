plugins {
    alias(libs.plugins.android.application)
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://artifacts.mercadolibre.com/repository/android-releases") }
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
    annotationProcessor(libs.compiler)    // Agregar CircleImageView para imágenes de perfil circulares
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // WebView para implementar nuestra propia integración con MercadoPago
    implementation("androidx.webkit:webkit:1.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    implementation("androidx.fragment:fragment:1.8.3")
    implementation(libs.fragment)
    implementation(libs.volley)

    // Cloudinary para subida de imágenes
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    
    // Para manejar JSON
    implementation("com.google.code.gson:gson:2.10.1")

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
