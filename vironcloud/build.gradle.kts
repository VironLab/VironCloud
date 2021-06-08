import java.nio.file.Files

dependencies {
    compileOnly(project(":vironcloud-common"))
    compileOnly(project(":vironcloud-api"))
}

tasks {
    jar {
        dependsOn(":vironcloud-client:build")
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
                from(File(project(":vironcloud-client").buildDir, "libs/vironcloud-client.jar").toPath())
                into(resources.toPath())
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

