import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    jacoco
    id("com.github.ben-manes.versions")
}

val applicationName = "DmsExplorer"
val versionMajor = 0
val versionMinor = 7
val versionPatch = 59

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "net.mm2d.dmsexplorer"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        base.archivesBaseName = "${applicationName}-${versionName}"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "d"
            isTestCoverageEnabled = true
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "${applicationName}-${versionName}.apk"
            }
        }
    }
    lintOptions {
        isAbortOnError = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.browser:browser:1.3.0")
    implementation("androidx.exifinterface:exifinterface:1.3.2")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("com.google.android.play:core:1.9.1")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")
    implementation("net.mm2d.mmupnp:mmupnp:3.1.3")
    implementation("net.mm2d.preference:preference:0.2.6")
    implementation("net.opacapp:multiline-collapsingtoolbar:27.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.20")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    debugImplementation("com.facebook.stetho:stetho:1.5.1")
    debugImplementation("com.facebook.stetho:stetho-okhttp3:1.5.1")

    testImplementation("junit:junit:4.13.1")
    testImplementation("io.mockk:mockk:1.10.5")
    testImplementation("com.google.truth:truth:1.1.2")
    testImplementation("org.robolectric:robolectric:4.5.1")
    testImplementation("androidx.test.ext:junit:1.1.2")
}

jacoco {
    toolVersion = "0.8.5"
}

tasks.create<JacocoReport>("jacocoTestReport") {
    group = "verification"
    dependsOn("testDebugUnitTest")
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
    sourceDirectories.setFrom("${projectDir}/src/main/java")
    classDirectories.setFrom(fileTree("${buildDir}/tmp/kotlin-classes/debug"))
    executionData.setFrom("${buildDir}/jacoco/testDebugUnitTest.exec")
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.toUpperCase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) }
}
