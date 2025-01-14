import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    id("com.google.cloud.tools.jib") version "3.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    application
}

group = "eu.glasskube.operator"

val javaOperatorVersion: String by project
val crdGeneratorVersion: String by project
val slf4jVersion: String by project
val logbackVersion: String by project
val jacksonVersion: String by project
val bouncyCastleVersion: String by project
val minioVersion: String by project

dependencies {
    implementation("io.javaoperatorsdk", "operator-framework", javaOperatorVersion)
    kapt("io.javaoperatorsdk", "operator-framework", javaOperatorVersion)
    kapt("io.fabric8", "crd-generator-apt", crdGeneratorVersion)

    implementation("org.slf4j", "slf4j-api", slf4jVersion)
    implementation("ch.qos.logback", "logback-core", logbackVersion)
    implementation("ch.qos.logback", "logback-classic", logbackVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
    implementation("org.bouncycastle", "bcpkix-jdk15to18", bouncyCastleVersion)
    implementation("io.minio", "minio", minioVersion)
    implementation("io.minio", "minio-admin", minioVersion)

    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "${JavaVersion.VERSION_17}"
    kotlinOptions.javaParameters = true
}

tasks.test {
    useJUnitPlatform()
}

jib {
    from {
        image = "ghcr.io/graalvm/jdk:ol9-java17-22.3.0"
    }

    to {
        image = "glasskube/operator"
        tags = setOf(version as String)
    }

    container {
        user = "333"
    }
}

application {
    mainClass.set("eu.glasskube.operator.MainKt")
}

gitProperties {
    keys = listOf(
        "git.branch",
        "git.build.version",
        "git.commit.id.abbrev",
        "git.commit.time"
    )
    dateFormat = "yyyy-MM-dd'T'HH:mmX"
}

tasks.create("clearCrd", Delete::class) {
    delete = setOf("../deploy/crd")
}

tasks.create("copyCrd", Copy::class) {
    dependsOn("kaptKotlin")
    dependsOn("clearCrd")
    from("build/tmp/kapt3/classes/main/META-INF/fabric8") {
        include("*glasskube.eu-v1.yml")
    }
    into("../deploy/crd")
}

tasks.findByName("classes")!!.dependsOn("copyCrd")

tasks.create("installCrd", Exec::class) {
    group = "kubernetes"
    dependsOn("copyCrd")
    commandLine(
        "kubectl",
        "apply",
        "-f",
        "../deploy/crd"
    )
}

tasks.create("loadImage", Exec::class) {
    group = "kubernetes"
    dependsOn("jibBuildTar")
    commandLine(
        "minikube",
        "image",
        "load",
        "build/jib-image.tar"
    )
}
