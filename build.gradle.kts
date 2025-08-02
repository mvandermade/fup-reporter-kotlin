import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("com.google.protobuf") version "0.9.4"
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

// Openfeign
extra["springCloudVersion"] = "2025.0.0"
extra["springGrpcVersion"] = "0.9.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")

    implementation("org.springframework.boot:spring-boot-starter-websocket")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")

    implementation("io.grpc:grpc-services")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testImplementation("org.testcontainers:kafka:1.21.3")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.springframework.grpc:spring-grpc-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
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
