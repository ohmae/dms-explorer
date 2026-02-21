import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kover)

    // for release
}

val applicationName = "DmsExplorer"
val versionMajor = 0
val versionMinor = 7
val versionPatch = 72

android {
    compileSdk {
        version = release(36)
    }

    namespace = "net.mm2d.dmsexplorer"
    defaultConfig {
        applicationId = "net.mm2d.dmsexplorer"
        minSdk = 23
        targetSdk = 36
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "d"
            enableAndroidTestCoverage = true
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint {
        abortOnError = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
    jvmToolchain(17)
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach {
            (it as VariantOutputImpl).outputFileName.set("$applicationName-${it.versionName.get()}.apk")
        }
    }
}

dependencies {
    implementation(libs.kotlinRefrect)
    implementation(libs.androidxAppCompat)
    implementation(libs.androidxCardview)
    implementation(libs.androidxConstraintLayout)
    implementation(libs.androidxPalette)
    implementation(libs.androidxRecyclerview)
    implementation(libs.androidxPreference)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxActivity)
    implementation(libs.androidxFragment)
    implementation(libs.androidxExifInterface)
    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxSwiperefreshlayout)
    implementation(libs.material)
    implementation(libs.playAppUpdate)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.mmupnp)
    implementation(libs.preference)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxJunit)

    // for release
}
