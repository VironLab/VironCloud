import kotlin.collections.*

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.20")
    }
}

//Define Plugins
plugins {
    id("java")
    id("maven")
    kotlin("jvm") version "1.5.10"
    kotlin("kapt") version "1.5.10"
    id("org.jetbrains.dokka") version "1.4.32"
}

//Define Variables for all Projects
allprojects {
    //Define Repositorys
    repositories {
        for (repo in eu.vironlab.vironcloud.gradle.Properties.repositories) {
            maven(repo)
        }
        for (repo in eu.vironlab.vironcloud.gradle.Properties.minecraftRepositories) {
            maven(repo)
        }
    }

    //Define Version and Group
    this.group = eu.vironlab.vironcloud.gradle.Properties.group
    this.version = eu.vironlab.vironcloud.gradle.Properties.version

}


//Default configuration for each module
subprojects {
    apply(plugin = "java")
    apply(plugin = "maven")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        compileOnly(eu.vironlab.vironcloud.gradle.getDependency("kotlin", "stdlib"))
        compileOnly(eu.vironlab.vironcloud.gradle.getDependency("kotlin", "serialization"))
        compileOnly(eu.vironlab.vironcloud.gradle.getDependency("kotlinx", "coroutines-core"))
        compileOnly(eu.vironlab.vironcloud.gradle.getDependency("google", "gson"))
        compileOnly(eu.vironlab.vironcloud.gradle.getDependency("google", "guice"))
        compileOnly(eu.vironlab.vironcloud.gradle.getDependency("vextension", "common"))
    }

    tasks {
        //Set the Name of the Sources Jar
        val sourcesJar by creating(Jar::class) {
            archiveFileName.set("${project.name}-sources.jar")
        }

        jar {
            archiveFileName.set("${project.name}.jar")
            doFirst {
                //Set Manifest
                manifest {
                    attributes["Implementation-Title"] = project.name
                    attributes["Implementation-Version"] = eu.vironlab.vironcloud.gradle.Properties.version
                    attributes["Specification-Version"] = eu.vironlab.vironcloud.gradle.Properties.version
                    attributes["Implementation-Vendor"] = "VironLab.eu"
                    attributes["Built-By"] = System.getProperty("user.name")
                    attributes["Build-Jdk"] = System.getProperty("java.version")
                    attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
                    attributes["VironLab-ProjectID"] = "vironcloud_v1"
                }
            }
            doLast {
                //Generate the Pom file for the Repository
                maven.pom {
                    withGroovyBuilder {
                        "project" {
                            groupId = eu.vironlab.vironcloud.gradle.Properties.group
                            artifactId = project.name
                            version = eu.vironlab.vironcloud.gradle.Properties.version
                            this.setProperty("inceptionYear", "2021")
                            "licenses" {
                                "license" {
                                    setProperty("name", "General Public License (GPL v3.0)")
                                    setProperty("url", "https://www.gnu.org/licenses/gpl-3.0.txt")
                                    setProperty("distribution", "repo")
                                }
                            }
                            "developers" {
                                "developer" {
                                    setProperty("id", "Infinity_dev")
                                    setProperty("name", "Florin Dornig")
                                    setProperty("email", "florin.dornig@gmx.de")
                                }
                            }
                        }
                    }

                }.writeTo("build/pom/pom.xml")
            }
        }

        compileKotlin {
            kotlinOptions.jvmTarget = "11"

        }
        withType<JavaCompile> {
            this.options.encoding = "UTF-8"
        }
    }


}