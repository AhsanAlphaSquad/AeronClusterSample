/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}


val generatedDir = file("${projectDir}/src/main/java")
val codecGeneration = configurations.create("codecGeneration")

dependencies {
    // Use JUnit test framework.
    testImplementation(libs.junit)

    // This dependency is used by the application.
    implementation(libs.guava)
    implementation("io.aeron:aeron-all:1.43.0")
    "codecGeneration"("uk.co.real-logic:sbe-tool:1.30.0")
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("aeron_cluster.App")
}

tasks.register<JavaExec>("generateCodecs") {
    group = "sbe"
    val codecsFile = "src/main/resources/sbe/protocol.xml"
    val sbeFile = "src/main/resources/sbe/sbe.xsd"
    inputs.files(codecsFile, sbeFile)
    outputs.dir(generatedDir)
    classpath = codecGeneration
    mainClass.set("uk.co.real_logic.sbe.SbeTool")
    args = listOf(codecsFile)
    systemProperties["sbe.output.dir"] = generatedDir
    systemProperties["sbe.target.language"] = "Java"
    systemProperties["sbe.validation.xsd"] = sbeFile
    systemProperties["sbe.validation.stop.on.error"] = "true"
    outputs.dir(generatedDir)
}
