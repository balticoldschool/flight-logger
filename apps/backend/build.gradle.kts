plugins {
    java
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.17.0"
}

group = "com.flightlogger"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // H2 Database
    runtimeOnly("com.h2database:h2")

    // Liquibase
    implementation("org.liquibase:liquibase-core")

    // OpenAPI Generator
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/api/openapi.yaml")
    outputDir.set("$buildDir/generated/api")
    apiPackage.set("com.flightlogger.backend.api")
    modelPackage.set("com.flightlogger.backend.model")

    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "generateModels" to "true",
        "useTags" to "true",
        "dateLibrary" to "java8",
        "useSpringBoot3" to "true",
        "openApiNullable" to "false", // necessary to disable use of JsonNullable which causes mapping errors
    ))
}

tasks.named("openApiGenerate") {
    inputs.dir("$rootDir/src/main/resources/api")
}

openApiValidate {
    inputSpec = "$rootDir/src/main/resources/api/openapi.yaml"
    recommend = true
}

sourceSets {
    main {
        java {
            srcDir("$buildDir/generated/api/src/main/java")
        }
    }
}
