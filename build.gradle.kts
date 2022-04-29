buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath(kotlin("gradle-plugin", version = "1.6.21"))
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")

        // for release
    }
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}
