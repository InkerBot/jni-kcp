import org.gradle.internal.jvm.Jvm

plugins {
    `binary-base`
}

tasks.create<Exec>("runCmake") {
    dependsOn(project(":").tasks["compileJava"])
    workingDir = File(buildDir, "cmake").apply { mkdirs() }

    environment("JAVA_HOME", Jvm.current().javaHome)

    commandLine("cmake", projectDir)
    outputs.upToDateWhen { false }
}

tasks.create<Exec>("runCmakeBuild") {
    dependsOn(tasks["runCmake"])
    workingDir = File(buildDir, "cmake").apply { mkdirs() }

    environment("JAVA_HOME", Jvm.current().javaHome)

    commandLine("make")
    outputs.file(File(buildDir, "cmake/libnative.dylib"))
    outputs.upToDateWhen { false }
}

tasks.assemble {
    dependsOn(tasks["runCmakeBuild"])
}