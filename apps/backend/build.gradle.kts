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

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/src/main/resources/api/openapi.yaml")
    outputDir.set("$buildDir/generated/api")
    apiPackage.set("com.flightlogger.backend")
    modelPackage.set("com.flightlogger.backend.model")

    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "generateModels" to "true",
        "useTags" to "true",
        "dateLibrary" to "java8",
        "useSpringBoot3" to "true"
    ))
}

openApiValidate {
    inputSpec = "$rootDir/src/main/resources/api/openapi.yaml"
    recommend = true
}

sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory}/generated/src/main/java")
        }
    }
}
