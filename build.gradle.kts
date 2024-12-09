import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "1.5.10"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.2.4"
    id("com.gradleup.shadow") version ("9.0.0-beta4")
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
}

dependencies {
    paperweight.paperDevBundle("${gameVersion}-R0.1-SNAPSHOT")

    library(kotlin("stdlib"))
    library("org.jetbrains.kotlinx:kotlinx-serialization-json:1.+")
    library("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.+")

    implementation("dev.jorel", "commandapi-bukkit-shade", "9.+")
    implementation("dev.jorel", "commandapi-bukkit-kotlin", "9.+")
    compileOnly("dev.jorel:commandapi-bukkit-core:9.+")

    implementation("net.kyori", "adventure-text-minimessage", "4.16.0")

    compileOnly("com.github.LoneDev6", "API-ItemsAdder", "3.6.1")
}

kotlin {
    jvmToolchain(17)
}

tasks {

    assemble {
        dependsOn(reobfJar)
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
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