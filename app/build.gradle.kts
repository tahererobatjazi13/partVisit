plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.dagger.hilt.android)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")

}
android {
    namespace = "com.partsystem.partvisitapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.partsystem.partvisitapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.ui.text.android)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //  Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //  Room Database
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.room.ktx)
    //  Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Dimen dp/sp
    implementation(libs.intuit.sdp)
    implementation(libs.intuit.ssp)

    // Date
    implementation(libs.jalali.calendar)

    // RxJava & RxAndroid
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.rxbinding)

    // Retrofit
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Shimmer
    implementation(libs.facebook.shimmer)
    implementation(libs.facebook.shimmer)

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    implementation("androidx.hilt:hilt-navigation-fragment:1.0.0")
}
