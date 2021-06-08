import eu.vironlab.vironcloud.gradle.getDependency

dependencies {
    compileOnly(project(":vironcloud-common"))
    compileOnly(project(":vironcloud-api"))
}

tasks {
    jar {
        for (proj in arrayOf("common", "api")) {
            dependsOn(":vironcloud-$proj:build")
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