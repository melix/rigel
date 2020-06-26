plugins {
    id("me.champeau.module")
    `java-test-fixtures`
    groovy
    `maven-publish`
    id("me.champeau.gradle.jmh") version "0.5.0"
}

repositories {
    jcenter()
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    testImplementation("org.codehaus.groovy:groovy-all:2.5.11")
    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("junit:junit:4.13")
}

tasks.named<JavaCompile>("compileJava9") {
    val mainOutput = tasks.named<JavaCompile>("compileJava").flatMap(JavaCompile::getDestinationDirectory)
    options.compilerArgs.addAll(listOf("--patch-module", "me.champeau.rigel=${mainOutput.get()}"))
}

publishing {
    repositories {
        maven {
            name = "Build"
            url = uri("$buildDir/repo")
        }
        publications {
            register<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}

jmh {
    isIncludeTests = false
    threads = 8
    fork = 4
    timeOnIteration = "3s"
    warmup = "3s"
    iterations = 1
    warmupIterations = 1
}