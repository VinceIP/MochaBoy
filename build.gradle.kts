plugins {
    application
    java
}

group = "org.mochaboy"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
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
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("org.mochaboy.MochaBoy")
}

tasks.test {
    useJUnitPlatform()
}