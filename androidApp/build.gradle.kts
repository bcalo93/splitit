import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val appVersionName: String = providers.gradleProperty("appVersionName").get()
val appVersionCode: Int = providers.gradleProperty("appVersionCode").get().toInt()

fun loadReleaseKeystoreProperties(): Properties? {
    val localFile = rootProject.file("keystore.properties")
    if (localFile.exists()) {
        return Properties().apply { localFile.inputStream().use { load(it) } }
    }
    val envFile = System.getenv("KEYSTORE_FILE")
    if (!envFile.isNullOrBlank()) {
        return Properties().apply {
            setProperty("storeFile", envFile)
            setProperty("storePassword", System.getenv("KEYSTORE_PASSWORD").orEmpty())
            setProperty("keyAlias", System.getenv("KEY_ALIAS").orEmpty())
            setProperty("keyPassword", System.getenv("KEY_PASSWORD").orEmpty())
        }
    }
    return null
}

val releaseKeystoreProperties = loadReleaseKeystoreProperties()

android {
    namespace = "com.splitit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.splitit"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (releaseKeystoreProperties != null) {
            create("release") {
                storeFile = rootProject.file(releaseKeystoreProperties.getProperty("storeFile"))
                storePassword = releaseKeystoreProperties.getProperty("storePassword")
                keyAlias = releaseKeystoreProperties.getProperty("keyAlias")
                keyPassword = releaseKeystoreProperties.getProperty("keyPassword")
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
}
