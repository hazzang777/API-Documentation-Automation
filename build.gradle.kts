import org.hidetake.gradle.swagger.generator.GenerateSwaggerUI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.7.10-SNAPSHOT"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("com.epages.restdocs-api-spec") version "0.12.0"
    id("org.hidetake.swagger.generator") version "2.18.2"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "me.hardy"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

val snippetsDir = file("${project.buildDir}/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.12.0")
    swaggerUI("org.webjars:swagger-ui:3.52.3")
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    test {
        outputs.dir(snippetsDir)
    }


    withType<GenerateSwaggerUI> {
        dependsOn("openapi3")
    }

    register<Copy>("copySwaggerUI") {
        dependsOn("generateSwaggerUIHelloService")

        val generateSwaggerTask = named<GenerateSwaggerUI>("generateSwaggerUIHelloService").get()
        from("${generateSwaggerTask.outputDir}")
        into("${project.buildDir}/resources/main/static")
    }

    withType<BootJar> {
        dependsOn("copySwaggerUI")
    }
}

openapi3 {
    setServer("http://localhost:8080")
    title = "TEST"
    description = "TEST"
    version = "1.0.0"
    format = "yaml"
}

swaggerSources {
    register("helloService").configure {
        setInputFile(file("${project.buildDir}/api-spec/openapi3.yaml"))
    }
}
