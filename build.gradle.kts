import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "2.1.10"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version ("9.0.0-beta4")
    kotlin("plugin.serialization") version "2.1.10"
}


group = properties["group"] as String
version = properties["version"] as String
description = properties["description"] as String

val gameVersion by properties
val foliaSupport = properties["foliaSupport"] as String == "true"
val projectName = properties["name"] as String
val commandAPIVersion = properties["commandAPIVersion"] as String

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://repo.flyte.gg/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

library(kotlin("stdlib"))
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

implementation("gg.flyte:twilight:1.1.18")

implementation("dev.jorel", "commandapi-bukkit-shade", "9.+")
implementation("dev.jorel", "commandapi-bukkit-kotlin", "9.+")
compileOnly("dev.jorel:commandapi-bukkit-core:9.+")

compileOnly("com.github.LoneDev6", "API-ItemsAdder", "3.6.1")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}

bukkit {
    main = "$group.${projectName.lowercase()}.${projectName}"
    apiVersion = "1.16"
    foliaSupported = foliaSupport
    // Optionals
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    depend = listOf()
    softDepend = listOf()
}