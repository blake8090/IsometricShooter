import org.gradle.kotlin.dsl.application

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    application
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("io.github.libktx:ktx-async:1.10.0-rc1")
    implementation("org.reflections:reflections:0.10.2")

    implementation("ch.qos.logback:logback-core:1.4.6")
    implementation("ch.qos.logback:logback-classic:1.4.6")
    implementation("org.slf4j:slf4j-api:2.0.7")

    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.11.0")
    implementation("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-desktop")
    implementation("space.earlygrey:shapedrawer:2.5.0")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.14.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.3.0")
}

application {
    mainClass.set("bke.iso.MainKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<JavaExec>("start") {
    group = "application"
    description = "First run tests, then run application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("bke.iso.MainKt")
    dependsOn("test")
}
