plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "org.mochaboy"
version = "1.0-SNAPSHOT"

javafx {
    version = "24.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
}

val javafxVersion = "24.0.1"
val os = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "win"
    org.gradle.internal.os.OperatingSystem.current().isLinux   -> "linux"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX  -> "mac"
    else -> error("Unsupported OS")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.lwjgl:lwjgl:3.3.2")
    implementation("org.lwjgl:lwjgl-opengl:3.3.2")
    implementation("org.lwjgl:lwjgl-glfw:3.3.2")
    runtimeOnly("org.lwjgl:lwjgl:3.3.2:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.2:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.2:natives-windows")

    implementation ("com.google.code.gson:gson:2.11.0")

    implementation("org.openjfx:javafx-base:$javafxVersion")
    implementation("org.openjfx:javafx-graphics:$javafxVersion")
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")

    runtimeOnly("org.openjfx:javafx-graphics:$javafxVersion:$os")
    runtimeOnly("org.openjfx:javafx-controls:$javafxVersion:$os")
    runtimeOnly("org.openjfx:javafx-fxml:$javafxVersion:$os")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(22))
}

application {
    mainModule.set("org.mochaboy")
    mainClass.set("org.mochaboy.MochaBoy")
}

tasks.test {
    useJUnitPlatform()
}