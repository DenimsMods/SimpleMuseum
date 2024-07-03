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

    mods {
        register(base.archivesName.get()) {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
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
        name = "ParchmentMC (Mappings)"
        content { includeGroup(libs.parchment.orNull?.group!!) }
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        officialMojangMappings()
        parchment(variantOf(libs.parchment) { artifactType("zip") })
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

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
