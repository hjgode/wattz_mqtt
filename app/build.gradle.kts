plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "dubrowgn.wattz"
        minSdk = 28 // BatteryManager.computeChargeTimeRemaining()
        // work around unused library resources
        resourceConfigurations.addAll(listOf("anydpi", "en", "es"))
        targetSdk = 34
        versionCode = 20
        versionName = "1.20"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "dubrowgn.wattz"
}

dependencies {

//    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0")
//    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
