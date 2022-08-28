/*
 * Reference for JVM projects:
 * https://docs.gradle.org/7.5.1/userguide/building_java_projects.html
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21" // Kotlin support

    // https://docs.gradle.org/current/userguide/java_library_plugin.html
    `java-library`

    antlr // Automatically compiles antlr dirs to sources
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom")) // Version-aligned Kotlin components
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8") // Kotlin JDK8 standard lib

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    antlr("org.antlr:antlr4:4.10.1")
}

tasks.named("compileKotlin") {
    dependsOn(":generateGrammarSource") // Compile ANTLR first
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}
