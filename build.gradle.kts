import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.serialization") version "1.3.61"
}

group = "io.github.fukkitmc"
version = "1.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx", "kotlinx-serialization-runtime", "0.14.0")
    api("org.ow2.asm", "asm", "7.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    repositories {
        maven {
            name = "Fukkit"
            url = uri("../fukkit-repo")
        }
    }

    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
