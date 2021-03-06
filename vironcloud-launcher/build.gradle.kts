import com.google.gson.GsonBuilder
import java.io.FileWriter
import java.nio.file.Files

val includeIntoLauncher by configurations.creating {
    setTransitive(false)
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

dependencies {
    includeIntoLauncher(eu.vironlab.vironcloud.gradle.getDependency("google", "gson"))
    includeIntoLauncher(eu.vironlab.vironcloud.gradle.getDependency("vextension", "common"))
}

tasks {
    jar {
        project(":vironcloud-modules").subprojects.forEach { proj ->
            if (!eu.vironlab.vironcloud.gradle.Properties.modules.contains(proj.name)) {
                eu.vironlab.vironcloud.gradle.Properties.modules.add(proj.name)
            }
        }
        for (file in includeIntoLauncher.files) {
            copy {
                from(zipTree(file))
                into("${project.buildDir}/classes/java/main")
            }
        }
        dependsOn(":vironcloud:build")
        val modules = project(":vironcloud-modules").subprojects
        modules.forEach { module ->
            val name = module.name
            dependsOn(":vironcloud-modules:$name:jar")
        }
        doFirst {
            val resources = File(buildDir, "resources/main").also {
                if (it.exists()) {
                    it.delete()
                }
            }
            Files.createDirectories(resources.toPath())
            copy {
                from(File(project(":vironcloud").buildDir, "libs/vironcloud.jar").toPath())
                into(resources.toPath())
            }
            modules.forEach { module ->
                copy {
                    from(
                        File(
                            project(":vironcloud-modules:${module.name}").buildDir,
                            "libs/${module.name}.jar"
                        ).toPath()
                    )
                    into(resources.toPath())
                }
            }
            val file = File(resources, "launch-info.json")
            val writer = FileWriter(file)
            writer.write(GsonBuilder().setPrettyPrinting().create().toJson(eu.vironlab.vironcloud.gradle.Properties.serialize()))
            writer.flush()
            writer.close()
            manifest {
                attributes["Main-Class"] = "eu.vironlab.vironcloud.launcher.Bootstrap"
            }
        }
    }
}
