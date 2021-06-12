plugins {
    id("java")
    kotlin("jvm") version "1.5.10"
}

//Define Repositorys
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.5.10")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "16"
    }
    withType<JavaCompile> {
        this.options.encoding = "UTF-8"
    }
}
