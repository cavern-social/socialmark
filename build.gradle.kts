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

tasks.generateGrammarSource {
    arguments = arguments + listOf(
        // Set a package header on the generated Java files
        "-package", "org.timmc.socialmark.internal",
        // Turn warnings into build-breaking errors
        "-Werror",
    )
    // For some reason, using separate Parser and Lexer files confuses the antlr
    // plugin and it starts dropping most of its files into src/main/gen
    // (except some are apparently duplicated into this generated-src path...)
    outputDirectory = File(buildDir, "generated-src/antlr/main/org/timmc/socialmark/internal")
}

sourceSets {
    main {
        java.srcDir("$buildDir/generated-src/antlr/main")
    }
}

tasks.compileKotlin {
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
