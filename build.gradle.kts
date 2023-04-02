version = "2.6.5"
group = "com.extendedclip.papi.expansion.server"

plugins {
    java
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("com.destroystokyo.paper", "paper-api", "1.13.2-R0.1-SNAPSHOT")
    compileOnly("me.clip", "placeholderapi", "2.11.3")
    implementation("org.jetbrains:annotations:24.0.0")
}

tasks {
    // Clean up the build folder
    build {
        doLast {
			// Delete all folders in the build directory besides libs
			file("build").listFiles()?.forEach {
				if (it.isDirectory && it.name != "libs") it.deleteRecursively()
			}
        }
    }

    // Text encoding
    compileJava {
        options.encoding = "UTF-8"
    }

    // Disable unnecessary tasks
    classes { enabled = false }
    compileTestJava { enabled = false }
    processTestResources { enabled = false }
    testClasses { enabled = false }
    test { enabled = false }
    check { enabled = false }
}
