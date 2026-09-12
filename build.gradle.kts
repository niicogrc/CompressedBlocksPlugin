plugins {
    java
}

group = "com.nico"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.74-stable")
}

tasks {
    compileJava {
        options.release.set(25)
        options.encoding = "UTF-8"
    }
    jar {
        archiveBaseName.set("CompressedBlocks")
    }
}
