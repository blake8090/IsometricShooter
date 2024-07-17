import org.gradle.kotlin.dsl.application
import io.github.fourlastor.construo.Target

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.0"
    id("io.gitlab.arturbosch.detekt").version("1.23.1")
    id("io.github.fourlastor.construo") version "1.2.0"
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

    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.4")

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

tasks.jar {
    manifest.attributes["Main-Class"] = application.mainClass
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

construo {
    name.set("IsometricShooter")
    humanName.set("IsometricShooter")

    jlink {
        modules.addAll("java.naming", "jdk.unsupported", "java.xml")
        guessModulesFromJar.set(false)
    }

    roast {
        // required by kotlinx-serialization in order to find polymorphic serializers
        useMainAsContextClassLoader.set(true)
    }

    targets {
        create<Target.Windows>("winX64") {
            architecture.set(Target.Architecture.X86_64)
            jdkUrl.set("https://github.com/adoptium/temurin20-binaries/releases/download/jdk-20.0.2%2B9/OpenJDK20U-jdk_x64_windows_hotspot_20.0.2_9.zip")
        }
    }
}
