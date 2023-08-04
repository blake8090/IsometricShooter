import org.gradle.kotlin.dsl.application

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.reflections:reflections:0.10.2")

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.11.0")
    implementation("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-desktop")
    implementation("io.github.libktx:ktx-async:1.12.0-rc1")
    implementation("space.earlygrey:shapedrawer:2.6.0")

    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.3.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.mockk:mockk:1.13.5")
}

application {
    mainClass.set("bke.iso.MainKt")
}

tasks.withType<Test>() {
    useJUnitPlatform()
}

tasks.register<JavaExec>("start") {
    group = "application"
    description = "First run tests, then run application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("bke.iso.MainKt")
    dependsOn("test")
}
