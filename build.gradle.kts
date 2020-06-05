plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.fukkitmc"
version = "2.1.3"

repositories {
    jcenter()
}

dependencies {
    api("org.ow2.asm", "asm", "8.0.1")
    compileOnly("com.google.code.gson", "gson", "2.8.6")
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
            artifact(tasks["sourcesJar"])
        }
    }
}
