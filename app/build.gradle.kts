import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Load version from properties file
val versionPropsFile = file("../version.properties")
val versionProperties = Properties()
if (versionPropsFile.exists()) {
    versionProperties.load(FileInputStream(versionPropsFile))
}
val currentVersionCode = (versionProperties["versionCode"] as String?)?.toIntOrNull() ?: 1
val currentVersionName = versionProperties["versionName"] as String? ?: "0.1.0"

android {
    namespace = "com.maxximum.alarmsetter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.maxximum.alarmsetter"
        minSdk = 35
        targetSdk = 36
        versionCode = currentVersionCode
        versionName = currentVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../maxximum.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEYSTORE_KEY_ALIAS") ?: "maxximum_key"
            keyPassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Task to auto-increment version code and patch version
tasks.register("incrementVersionCode") {
    doLast {
        val versionPropsFile = file("../version.properties")
        val versionProperties = Properties()
        if (versionPropsFile.exists()) {
            versionProperties.load(FileInputStream(versionPropsFile))
        }
        
        // Increment patch version (last number in semantic versioning)
        val currentVersionName = versionProperties["versionName"] as String? ?: "0.1.0"
        val versionParts = currentVersionName.split(".").toMutableList()
        val patch = versionParts.last().toInt()
        versionParts[versionParts.size - 1] = (patch + 1).toString()
        val newVersionName = versionParts.joinToString(".")
        
        // Increment versionCode
        val currentCode = (versionProperties["versionCode"] as String?)?.toIntOrNull() ?: 1
        val newCode = currentCode + 1
        
        versionProperties["versionName"] = newVersionName
        versionProperties["versionCode"] = newCode.toString()
        versionProperties.store(versionPropsFile.outputStream(), "Auto-incremented version")
        println("Version incremented: $currentVersionName -> $newVersionName (code: $currentCode -> $newCode)")
    }
}

// Hook the increment task to run after assembleRelease (after Android plugin creates tasks)
afterEvaluate {
    tasks.named("assembleRelease").configure {
        finalizedBy("incrementVersionCode")
    }
}