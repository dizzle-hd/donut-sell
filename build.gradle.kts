plugins {
    id("net.fabricmc.fabric-loom") version "1.19.4"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()
base.archivesName = providers.gradleProperty("archives_base_name").get()

repositories {
    maven("https://repo.polyfrost.org/releases") { content { includeGroup("org.polyfrost.oneconfig") } }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    // OneConfig is provided at runtime by OneClient / the OneConfig mod, so it is never bundled.
    if (property("oneconfig_use_maven").toString().toBoolean()) {
        compileOnly("org.polyfrost.oneconfig:config-impl:${property("oneconfig_version")}")
    } else {
        compileOnly(project(":oneconfig-stubs"))
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version.toString(),
        "loader_version" to providers.gradleProperty("loader_version").get(),
        "minecraft_version" to providers.gradleProperty("minecraft_version").get(),
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}
