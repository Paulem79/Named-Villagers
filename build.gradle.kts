import dev.s7a.gradle.minecraft.server.tasks.LaunchMinecraftServerTask
import ovh.paulem.buildscript.NewGithubChangelog
import proguard.gradle.ProGuardTask

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.8"

    id("com.modrinth.minotaur") version "2.8.7"

    id("dev.s7a.gradle.minecraft.server") version "3.2.1"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.+") {
            exclude("com.android.tools.build")
        }
    }
}

group = "ovh.paulem.namedvillagers"
version = "1.2"

// ------------------------ REPOSITORIES ------------------------
repositories {
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven {
        name = "jeffMediaPublic"
        url = uri("https://repo.jeff-media.com/public")
    }

    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/central") }

    maven("https://repo.dmulloy2.net/repository/public/")
}

// ------------------------ DEPENDENCIES ------------------------
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.14.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("commons-io:commons-io:2.19.0")

    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")

    implementation("org.bstats:bstats-bukkit:3.1.0")

    implementation("com.jeff_media:SpigotUpdateChecker:3.0.4") {
        exclude(group = "com.github.Anon8281", module = "UniversalScheduler")
        exclude(group = "com.jeff_media.updatechecker.universalScheduler")
    }
    implementation("com.github.Anon8281:UniversalScheduler:0.1.7")
}

// ------------------------ PROGUARD ------------------------
tasks.register<ProGuardTask>("proguardJar") {
    outputs.upToDateWhen { false }
    dependsOn(tasks.shadowJar)
    configuration("proguard-rules.pro")

    injars(tasks.shadowJar)
    outjars(file("build/libs/temp-${tasks.shadowJar.get().archiveFileName.get()}"))

    finalizedBy("finalizeJar")
}

// Rename the final proguard jar to the original shadowJar name
tasks.register("finalizeJar") {
    dependsOn("proguardJar")
    doLast {
        val shadowJarFile = tasks.shadowJar.get().archiveFile.get().asFile
        val proguardedJarFile = file("build/libs/temp-${tasks.shadowJar.get().archiveFileName.get()}")

        shadowJarFile.delete()
        proguardedJarFile.renameTo(shadowJarFile)
    }
}

// ------------------------ SHADOW JAR ------------------------
artifacts.archives(tasks.shadowJar)

tasks.shadowJar {
    archiveClassifier.set("")

    exclude("META-INF/**")
    exclude("LICENSE.txt")
    exclude("License-ASM.txt")

    relocate("org.bstats", "ovh.paulem.namedvillagers.libs.bstats")
    relocate("com.jeff_media.updatechecker", "ovh.paulem.namedvillagers.libs.updatechecker")

    // Use UniversalScheduler from SpigotUpdateChecker instead of the one from implementation
    exclude("com/github/Anon8281/universalScheduler/*Scheduler/**")
    exclude("com/github/Anon8281/universalScheduler/scheduling/**")
    exclude("com/github/Anon8281/universalScheduler/utils/**")
    exclude("com/github/Anon8281/universalScheduler/UniversalScheduler.**")

    relocate("com.github.Anon8281.universalScheduler", "ovh.paulem.namedvillagers.libs.updatechecker.universalScheduler")

    minimize()
}

// ------------------------ RESOURCES PROCESS ------------------------
tasks.processResources {
    inputs.property("version", version)

    filesMatching("plugin.yml") {
        expand(mapOf("version" to version))
    }
}

// ------------------------ PAPER TEST SYSTEM ------------------------
val paperDir = rootDir.resolve("servers").resolve("paper")

listOf("1.14.1", "1.14.4", "1.18", "1.21", "1.21.4", "1.21.5").forEach { version ->
    tasks.register<LaunchMinecraftServerTask>("paper-$version") {
        dependsOn("finalizeJar")

        doFirst {
            copies(version, paperDir)
        }

        serverDirectory.set(paperDir.resolve(version).absolutePath)
        jarUrl.set(LaunchMinecraftServerTask.JarUrl.Paper(version))
        agreeEula.set(true)
    }
}

val foliaDir = rootDir.resolve("servers").resolve("folia")

listOf("1.21.4").forEach { version ->
    tasks.register<LaunchMinecraftServerTask>("folia-$version") {
        dependsOn("finalizeJar")

        doFirst {
            copies(version, foliaDir)
        }

        serverDirectory.set(foliaDir.resolve(version).absolutePath)
        jarUrl.set(LaunchMinecraftServerTask.JarUrl.Folia(version))
        agreeEula.set(true)
    }
}

private fun copies(version: String, workDir: File) {
    delete {
        delete(fileTree(workDir.resolve("$version/plugins")).filter {
            it.isFile() && it.extension == "jar" && it.parentFile == workDir.resolve("$version/plugins")
        })
    }

    // Copy the ops.json file to the server directory
    copy {
        from(rootDir.resolve("resources").resolve("ops.json"))
        into(workDir.resolve(version))
    }

    // Copy the jar file to the plugins directory
    copy {
        from(tasks.shadowJar.get().archiveFile.get().asFile.absolutePath)
        into(workDir.resolve("$version/plugins"))
    }

    // Copy the plugins to the plugins directory
    copy {
        from(fileTree(rootDir.resolve("resources")).filter {
            it.isFile() && it.extension == "jar" && it.nameWithoutExtension.startsWith("pl-")
        })
        into(workDir.resolve("$version/plugins"))
    }
}

// ------------------------ MODRINTH ------------------------
tasks.modrinth {
    dependsOn(tasks.build)
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("SbGHNzYA")
    versionNumber.set(project.version.toString())
    versionName.set("Villagers With Names ${project.version}")
    versionType.set("release")
    changelog.set(NewGithubChangelog.getChangelog())
    uploadFile.set(tasks.shadowJar.get().archiveFile.get().asFile)
    gameVersions.addAll(listOf("1.21.7", "1.21.6", "1.21.5", "1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21", "1.20.6", "1.20.5", "1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20", "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19", "1.18.2", "1.18.1", "1.18", "1.17.1", "1.17", "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1", "1.16", "1.15.2", "1.15.1", "1.15", "1.14.4", "1.14.3", "1.14.2", "1.14.1"))
    loaders.addAll(listOf("bukkit", "folia", "paper", "purpur", "spigot"))
}

// ------------------------ MISC ------------------------
tasks.register<Task>("changelog") {
    doLast {
        val changelog = NewGithubChangelog.getChangelog()
        println(changelog)
    }
}

tasks.withType<JavaCompile>().configureEach {
    JavaVersion.VERSION_1_8.toString().also {
        sourceCompatibility = it
        targetCompatibility = it
    }
    options.encoding = "UTF-8"
}

tasks.build {
    mustRunAfter(tasks.clean)
    dependsOn(tasks.clean)

    dependsOn("proguardJar")
}

java {
    withSourcesJar()
}

tasks.jar { enabled = false }

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}