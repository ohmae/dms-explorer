pluginManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google().content {
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
            includeGroupAndSubgroups("androidx")
        }
        gradlePluginPortal().content {
            includeGroupAndSubgroups("com.github.ben-manes")
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

rootProject.name = "dms-explorer"
include(":mobile")
include(":tv")
