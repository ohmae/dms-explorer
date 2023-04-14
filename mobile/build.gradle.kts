import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.Locale

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    jacoco
    id("com.github.ben-manes.versions")

    // for release
}

val applicationName = "DmsExplorer"
val versionMajor = 0
val versionMinor = 7
val versionPatch = 64

android {
    compileSdk = 33

    namespace = "net.mm2d.dmsexplorer"
    defaultConfig {
        applicationId = "net.mm2d.dmsexplorer"
        minSdk = 21
        targetSdk = 33
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        base.archivesName.set("${applicationName}-${versionName}")
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all {
                (this as BaseVariantOutputImpl).outputFileName = "${applicationName}-${versionName}.apk"
            }
        }
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        dataBinding = true
        viewBinding = true
    }
    lint {
        abortOnError = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.browser:browser:1.5.0")
    implementation("androidx.activity:activity-ktx:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.5.6")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.android.play:core:1.10.3")
    implementation("com.google.android.play:core-ktx:1.8.1")
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("net.mm2d.mmupnp:mmupnp:3.1.6")
    implementation("net.mm2d.preference:preference:0.3.0")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.robolectric:robolectric:4.10")
    testImplementation("androidx.test.ext:junit:1.1.5")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")
    debugImplementation("com.facebook.flipper:flipper:0.189.0")
    debugImplementation("com.facebook.soloader:soloader:0.10.5")
    debugImplementation("com.facebook.flipper:flipper-network-plugin:0.189.0")
    debugImplementation("com.facebook.flipper:flipper-leakcanary2-plugin:0.189.0")

    // for release
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.create<JacocoReport>("jacocoTestReport") {
    group = "verification"
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    sourceDirectories.setFrom("${projectDir}/src/main/java")
    classDirectories.setFrom(fileTree("${buildDir}/tmp/kotlin-classes/debug"))
    executionData.setFrom("${buildDir}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.uppercase(Locale.getDefault())
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    rejectVersionIf { !isStable(candidate.version) }
}
