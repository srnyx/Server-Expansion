description = "PAPI-Expansion-Server"
version = "2.6.4"
group = "com.extendedclip.papi.expansion.server"

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
}


tasks.compileJava {
    options.encoding = "UTF-8"
}