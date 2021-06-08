dependencies {
    compileOnly(project(":vironcloud-common"))
}

tasks {
    jar {
        dependsOn(":vironcloud-common:build")
        val buildDir = project(":vironcloud-common").buildDir.path
        from("$buildDir/classes/kotlin/main") {
            include("**")
        }
        from("$buildDir/resources/main") {
            include("**")
        }
    }
}
