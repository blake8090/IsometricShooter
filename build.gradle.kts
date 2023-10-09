import org.gradle.kotlin.dsl.application

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.0"
    id("io.gitlab.arturbosch.detekt").version("1.23.1")
    application
}

kotlin {
    jvmToolchain(20)
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.1")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.reflections:reflections:0.10.2")

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.0")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.0:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype:1.12.0")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:1.12.0:natives-desktop")
    implementation("com.badlogicgames.gdx-controllers:gdx-controllers-core:2.2.3")
    implementation("com.badlogicgames.gdx-controllers:gdx-controllers-desktop:2.2.3")
    implementation("io.github.libktx:ktx-async:1.12.0-rc1")
    implementation("space.earlygrey:shapedrawer:2.6.0")

    implementation("org.lwjgl:lwjgl-nfd:3.3.2")
    runtimeOnly("org.lwjgl:lwjgl-nfd:3.3.2:natives-windows")

    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.mockk:mockk:1.13.5")
}

application {
    mainClass.set("bke.iso.MainKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<JavaExec>("start") {
    group = "application"
    description = "First run tests, then run application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("bke.iso.MainKt")
    dependsOn("test")
}
