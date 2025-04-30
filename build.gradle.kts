plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.1"
val springModulithVersion by extra("1.3.5")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    implementation("org.springframework.modulith:spring-modulith-events-kafka")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.modulith:spring-modulith-bom:$springModulithVersion")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Copy>("copyPreCommitHook") {
    description = "Copy pre-commit git hook from the scripts to the .git/hooks folder."
    group = "githooks"
    outputs.upToDateWhen { false }
    filePermissions {
        user {
            read = true
            execute = true
        }
        other.execute = false
    }
    from("$rootDir/scripts/pre-commit")
    into("$rootDir/.git/hooks/")
}
