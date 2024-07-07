@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.fabric.loom)
}

version = "0.1.0"
group = "dev.denimred"

base {
    archivesName = "simplemuseum"
}

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath = file("src/main/resources/simplemuseum.accesswidener")

    mods.register(base.archivesName.get()) {
        sourceSet(sourceSets["main"])
        sourceSet(sourceSets["client"])
    }

    log4jConfigs.from(file("./src/log4j.xml"))

    runs.getByName("client") {
        programArg("--username=DenimRed")
        programArg("--uuid=2f6fe476323e4ede945e927a34d38fe9")
        programArg("--width=1280")
        programArg("--height=720")
    }
}

fabricApi {
    configureDataGeneration {
        createSourceSet = true
        strictValidation = true
    }
}

repositories {
    maven("https://maven.parchmentmc.net/") {
        name = "ParchmentMC"
        content { includeGroup(libs.parchment.orNull?.group!!) }
    }
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
        }
    }
    maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

dependencies {
    // Minecraft
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment(variantOf(libs.parchment) { artifactType("zip") })
    })
    // Fabric
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    // GeckoLib
    modImplementation(libs.geckolib)
    implementation(libs.mclib)
    // Misc
    compileOnly(libs.jsr305) // JSR 305 is cringe but so am I, so it's okay
    modRuntimeOnly(libs.modmenu)
    modRuntimeOnly("maven.modrinth:jade:CciLEAMK") // ID instead of version since it uses the Forge release otherwise
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
    }
}
