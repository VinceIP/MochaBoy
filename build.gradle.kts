plugins {
    application
    java
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "org.mochaboy"
version = "1.0-SNAPSHOT"

javafx {
    // Use JavaFX built for JDK 21 so the tests can run on the CI JDK
    version = "21.0.2"
    modules = listOf("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
}

// JavaFX 24 requires JDK 22. Use the latest JavaFX 21 build so tests run on JDK 21.
val javafxVersion = "21.0.2"
val os = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "win"
    org.gradle.internal.os.OperatingSystem.current().isLinux -> "linux"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "mac"
    else -> error("Unsupported OS")
}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    // allow compiling against junit modules when building the main module
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.10.2")
    compileOnly("org.junit.jupiter:junit-jupiter-params:5.10.2")
    compileOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    compileOnly(platform("org.junit:junit-bom:5.10.2"))
    compileOnly("org.junit.jupiter:junit-jupiter-api")
    compileOnly("org.junit.jupiter:junit-jupiter-params")

    implementation("org.lwjgl:lwjgl:3.3.2")
    implementation("org.lwjgl:lwjgl-opengl:3.3.2")
    implementation("org.lwjgl:lwjgl-glfw:3.3.2")
    runtimeOnly("org.lwjgl:lwjgl:3.3.2:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.2:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.2:natives-windows")

    implementation("com.google.code.gson:gson:2.11.0")

    implementation("org.openjfx:javafx-base:$javafxVersion")
    implementation("org.openjfx:javafx-graphics:$javafxVersion")
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")

    runtimeOnly("org.openjfx:javafx-graphics:$javafxVersion:$os")
    runtimeOnly("org.openjfx:javafx-controls:$javafxVersion:$os")
    runtimeOnly("org.openjfx:javafx-fxml:$javafxVersion:$os")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
application {
    mainModule.set("org.mochaboy")
    mainClass.set("org.mochaboy.MochaBoy")
}

tasks.test {
    useJUnitPlatform()
    // Running tests on the module-path requires opening our modules
    jvmArgs(
        "--add-opens", "org.mochaboy/org.mochaboy=ALL-UNNAMED",
        "--add-opens", "org.mochaboy/org.mochaboy.opcode=ALL-UNNAMED",
        "--add-opens", "org.mochaboy/org.mochaboy.gui.fx=ALL-UNNAMED"
    )
    extensions.configure(org.javamodularity.moduleplugin.extensions.TestModuleOptions::class) {
        runOnClasspath = true
    }
}

tasks.compileTestJava {
    extensions.configure(org.javamodularity.moduleplugin.extensions.CompileTestModuleOptions::class) {
        setCompileOnClasspath(true)
    }
}