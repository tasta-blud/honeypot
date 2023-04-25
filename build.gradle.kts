import kotlin.io.path.div

plugins {
    kotlin("js") version "1.8.21"
    id("de.comahe.i18n4k") version "0.5.0"
}

group = "honeypot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("de.comahe.i18n4k:i18n4k-core-js:0.5.0")
    api("nl.astraeus:kotlin-css-generator:1.0.2")
    testImplementation(kotlin("test"))
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                devtool = "inline-cheap-module-source-map"
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
}

i18n4k {
    sourceCodeLocales = listOf("en", "ru")
}

tasks.named("generateI18n4kFiles") {
    dependsOn(tasks.named("kotlinSourcesJar"))
}

tasks.named<Copy>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.WARN
    dependsOn(tasks.named("generateI18n4kFiles"))
}

tasks.register<Zip>("package") {
    group = "package"
    archiveFileName.set("${project.name}.zip")
    dependsOn("browserProductionWebpack")
    from(buildDir.toPath() / "distributions")
    destinationDirectory.set((buildDir.toPath() / "package").toFile())
    exclude("*.zip")
}

tasks.named("build") {
    finalizedBy(tasks.named("package"))
}
