//Repositorys for Plugins
pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        mavenCentral()
    }
}

rootProject.name = "VironCloud"


include(":vironcloud-api")
include(":vironcloud")
include(":vironcloud-launcher")
include(":vironcloud-common")
include(":vironcloud-modules")
include(":vironcloud-client")
include(":vironcloud-modules:module-proxy")

