plugins {
    java
}

allprojects {
    group = "io.github.hongyuncloud"
    version = "1.0-SNAPSHOT"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.netty:netty-all:4.1.97.Final")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.compilerArgs.addAll(arrayOf(
        "-h",
        project(":native").buildDir.resolve("generated/sources/headers").toString()
    ))
}

tasks.processResources {
    dependsOn(project(":native").tasks["runCmakeBuild"])
    from(project(":native").tasks["runCmakeBuild"])
}