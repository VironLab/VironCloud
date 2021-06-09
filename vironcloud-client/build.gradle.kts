import java.io.FileWriter
import java.nio.file.Files

val includeIntoClient by configurations.creating {
    setTransitive(false)
}


dependencies {
    includeIntoClient(eu.vironlab.vironcloud.gradle.getDependency("google", "gson"))
    compileOnly(project(":vironcloud-common"))
    compileOnly(project(":vironcloud-api"))
    compileOnly("com.velocitypowered:velocity-api:1.1.8")
    kapt("com.velocitypowered:velocity-api:1.1.8")
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    kapt("org.spongepowered:spongeapi:7.2.0")
}

tasks {
    jar {
        for (proj in arrayOf("common", "api")) {
            dependsOn(":vironcloud-$proj:build")
        }
        doFirst {
            val resources = File(buildDir, "resources/main").also {
                if (it.exists()) {
                    it.delete()
                }
            }
            Files.createDirectories(resources.toPath())
            copy {
                from(zipTree(includeIntoClient.singleFile))
                into("${project.buildDir}/classes/java/main")
            }
            val file = File(resources, "client-launch-info.json")
            val writer = FileWriter(file)
            writer.write(
                com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    .toJson(eu.vironlab.vironcloud.gradle.Properties.serializeForClient())
            )
            writer.flush()
            writer.close()
            manifest {
                attributes["Main-Class"] = "eu.vironlab.vironcloud.client.boot.CloudClientBootstrap"
            }
        }
        for (proj in arrayOf("common", "api")) {
            val buildDir = project(":vironcloud-$proj").buildDir.path
            from("$buildDir/classes/kotlin/main") {
                include("**")
            }
            from("$buildDir/resources/main") {
                include("**")
            }
        }
    }
}