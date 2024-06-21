plugins {
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4"
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
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
