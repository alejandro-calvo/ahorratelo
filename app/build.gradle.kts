import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // Activamos kapt para que Room pueda generar sus clases internas.
    id("org.jetbrains.kotlin.kapt")
    // Plugin que lee GOOGLE_MAPS_API_KEY desde local.properties.
    // Es el mismo metodo que en el proyecto mapas.
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.ahorratelo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ahorratelo"
        minSdk = 24
        targetSdk = 36
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Activamos DataBinding como aparece en los apuntes.
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // RecyclerView para mostrar la lista de gastos.
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Room: base de datos local.
    implementation("androidx.room:room-runtime:2.8.3")
    kapt("androidx.room:room-compiler:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")

    // LiveData para observar cambios de datos.
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // ViewModel con soporte para viewModelScope.
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Corrutinas en Android.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Google Maps.
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:android-maps-utils:2.2.0")

    // Localización del dispositivo.
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}