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
    }
}

rootProject.name = "Eye_track_APP"
include(":app")

// 添加 UVCCamera 模块
include(":usbCameraCommon")
include(":libuvccamera")

// 设置 UVCCamera 模块路径（假设放在项目根目录的 libs 文件夹中）
project(":usbCameraCommon").projectDir = file("libs/UVCCamera/usbCameraCommon")
project(":libuvccamera").projectDir = file("libs/UVCCamera/libuvccamera")
