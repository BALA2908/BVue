pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Required for NewPipeExtractor (rule 0 of the master brief)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "BVue"
include(":app")
