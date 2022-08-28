/*
 * Reference for JVM projects:
 * https://docs.gradle.org/7.5.1/userguide/building_java_projects.html
 */

plugins {
    // https://docs.gradle.org/current/userguide/java_library_plugin.html
    `java-library`

    antlr // Automatically compiles antlr dirs to sources
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.10.1")
}
