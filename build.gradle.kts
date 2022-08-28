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
    // The ANTLR plugin will generate .java files at a location corresponding to
    // the .g4 files. It also only looks for .g4 files at the root dir of
    // `src/main/antlr`. However, I need the Java files to be in a package. So,
    // there are two options:
    //
    // A. Put the grammar files directly in `src/main/antlr` and use an explicit
    //    `outputDirectory` to drop them into their package hierarchy.
    // B. Put the grammar files into a package hierarchy and specify the `-lib`
    //    antlr CLI option to tell it to look in that directory.
    outputDirectory = File(buildDir, "generated-src/antlr/main/org/timmc/socialmark/internal")
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
