plugins {
    `java-library`
}

val java9 by sourceSets.creating {
    java {
        srcDir("src/main/java9")
    }
}

tasks.named("compileJava9Java").configure {
    this as JavaCompile
    sourceCompatibility = "9"
    targetCompatibility = "9"
}

tasks {
    jar {
        from(java9.output) {
            into("META-INF/versions/9")
            include("module-info.class")
        }
    }
}