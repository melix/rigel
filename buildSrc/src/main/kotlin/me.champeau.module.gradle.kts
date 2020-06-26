plugins {
    `java-library`
}

java {
	configure {
		registerJvmLanguageSourceDirectory(sourceSets.main.get(), "java9") {
			withDescription("Java 9 main sources")
			compiledWithJava {
				sourceCompatibility = "9"
    			targetCompatibility = "9"
			}
		}
	}
}

