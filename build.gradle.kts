import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.paper
import xyz.srnyx.gradlegalaxy.utility.setupJava


plugins {
    java
    id("xyz.srnyx.gradle-galaxy") version "1.0.2"
}

setupJava("com.extendedclip.papi.expansion.server", "2.6.5")
paper("1.13.2")
repository(Repository.PLACEHOLDER_API)
dependencies.compileOnly("me.clip", "placeholderapi", "2.11.3")
