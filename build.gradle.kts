import kotlin.collections.*
import eu.vironlab.vironcloud.gradle.*

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
    id("maven-publish")
    kotlin("jvm") version "1.5.10"
    kotlin("kapt") version "1.5.10"
    id("org.jetbrains.dokka") version "1.4.32"
}
//Configure build of docs
tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(File(rootProject.buildDir.path, "vextension-v2.0.0"))
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
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        compileOnly(getDependency("kotlin", "stdlib"))
        compileOnly(getDependency("kotlin", "serialization"))
        compileOnly(getDependency("kotlinx", "coroutines-core"))
        compileOnly(getDependency("google", "gson"))
        compileOnly(getDependency("google", "guice"))
        compileOnly(getDependency("vextension", "common"))
    }

    if (System.getProperty("publishName") != null && System.getProperty("publishPassword") != null) {
        publishing {
            publications {
                create<MavenPublication>(project.name) {
                    artifact("${project.buildDir}/libs/${project.name}-sources.jar") {
                        extension = "sources"
                    }
                    artifact("${project.buildDir}/libs/${project.name}.jar") {
                        extension = "jar"
                    }
                    groupId = Properties.group
                    artifactId = project.name
                    version = Properties.version
                    pom {
                        name.set(project.name)
                        url.set("https://github.com/VironLab/VironCloud")
                        properties.put("inceptionYear", "2021")
                        licenses {
                            license {
                                name.set("General Public License (GPL v3.0)")
                                url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("Infinity_dev")
                                name.set("Florin Dornig")
                                email.set("infinitydev@vironlab.eu")
                            }
                            developer {
                                id.set("SteinGaming")
                                name.set("Danial Daryab")
                                email.set("steingaming@vironlab.eu")
                            }
                        }
                    }
                }
                repositories {
                    maven("https://repo.vironlab.eu/repository/maven-snapshot/") {
                        this.name = "vironlab-snapshot"
                        credentials {
                            this.password = System.getProperty("publishPassword")
                            this.username = System.getProperty("publishName")
                        }
                    }
                }
            }
        }
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
        }

        compileKotlin {
            kotlinOptions.jvmTarget = "16"
        }
        withType<JavaCompile> {
            this.options.encoding = "UTF-8"
        }
    }

}