pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Allow local AAR/JAR lookup from module-local libs folders.
        // Add any paths that will contain .aar/.jar files.
        // I recommend putting vendor artifacts into rfid/libs/
        flatDir {
            // directories are relative to the project root
            dirs("rfid/libs")
        }
    }
}

rootProject.name = "Prototype"
include(":app")
include(":rfid-core")
include(":rfid-chainway")
include(":rfid-urovo")
