pluginManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google().content {
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
            includeGroupAndSubgroups("androidx")
        }
        gradlePluginPortal().content {
            includeGroupAndSubgroups("org.gradle.toolchains")
        }
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google().content {
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
            includeGroupAndSubgroups("androidx")
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

rootProject.name = "dms-explorer"
include(":mobile")
