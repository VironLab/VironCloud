subprojects {
    dependencies {
        compileOnly(project(":vironcloud"))
        kapt(project(":vironcloud"))
        compileOnly(project(":vironcloud-common"))
        compileOnly(project(":vironcloud-api"))
    }
}