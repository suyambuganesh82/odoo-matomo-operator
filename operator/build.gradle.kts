import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    id("com.google.cloud.tools.jib") version "3.3.0"
    application
}

group = "eu.glasskube.operator"
version = "0.1-SNAPSHOT"

val javaOperatorVersion: String by project
val crdGeneratorVersion: String by project
val slf4jVersion: String by project
val jacksonVersion : String by project

dependencies {
    implementation("io.javaoperatorsdk", "operator-framework", javaOperatorVersion)
    kapt("io.javaoperatorsdk", "operator-framework", javaOperatorVersion)
    kapt("io.fabric8", "crd-generator-apt", crdGeneratorVersion)

    implementation("org.slf4j", "slf4j-jdk14", slf4jVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)

    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_11}"
    kotlinOptions.javaParameters = true
}

tasks.test {
    useJUnitPlatform()
}

jib  {
    to {
        image = "glasskube-operator"
        tags = setOf(version as String)
    }
}

application {
    mainClass.set("eu.glasskube.operator.MainKt")
}

tasks.create("installCrd", Exec::class) {
    group = "kubernetes"
    dependsOn("build")
    commandLine(
        "kubectl",
        "apply",
        "-f",
        "build/tmp/kapt3/classes/main/META-INF/fabric8/*-v1.yml"
    )
}