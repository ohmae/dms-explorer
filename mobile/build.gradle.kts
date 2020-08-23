import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import groovy.lang.Closure

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
val versionPatch = 57

android {
    compileSdkVersion(29)

    defaultConfig {
        applicationId = "net.mm2d.dmsexplorer"
        minSdkVersion(21)
        targetSdkVersion(29)
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
        @Suppress("UNCHECKED_CAST")
        unitTests.all(closureOf<Test> {
            extensions.configure(JacocoTaskExtension::class.java) {
                isIncludeNoLocationClasses = true
            }
        } as Closure<Test>)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.preference:preference:1.1.1")
    implementation("androidx.browser:browser:1.2.0")
    implementation("androidx.exifinterface:exifinterface:1.2.0")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("com.google.android.material:material:1.2.0")
    implementation("com.google.android.play:core:1.8.0")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation("net.mm2d:mmupnp:3.1.1")
    implementation("net.mm2d:preference:0.2.5")
    implementation("net.opacapp:multiline-collapsingtoolbar:27.1.1")
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    debugImplementation("com.facebook.stetho:stetho:1.5.1")
    debugImplementation("com.facebook.stetho:stetho-okhttp3:1.5.1")

    testImplementation("junit:junit:4.13")
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("com.google.truth:truth:1.0.1")
    testImplementation("org.robolectric:robolectric:4.3.1")
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
