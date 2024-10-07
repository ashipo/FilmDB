plugins {
    alias(libs.plugins.boot) apply false
    alias(libs.plugins.dependency.management)
    id("java")
}

group = "com.demo"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation(libs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}
