description = "PAPI-Expansion-Server"
version = "2.6.3"
group = "com.extendedclip.papi.expansion.server"

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("me.clip:placeholderapi:2.10.11")
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.1")
}

plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application.mainClass.set("io.ktor.server.netty.EngineMain")

tasks.compileJava {
    options.encoding = "UTF-8"
}