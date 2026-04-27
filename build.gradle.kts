import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
}

allprojects {
    group = "com.woocurlee"
    version = "0.0.1"
    description = "grpc-checkout"

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

object Version {
    const val GRPC = "1.0.2"
    const val KOTEST = "6.0.2"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${Version.GRPC}")
    }
}

subprojects {
    if (project.name != "proto") {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "org.jetbrains.kotlin.plugin.spring")
        apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
        apply(plugin = "org.jetbrains.kotlin.jvm")

        dependencies {
            implementation(project(":proto"))

            implementation("org.springframework.boot:spring-boot-starter")
            implementation("org.jetbrains.kotlin:kotlin-reflect")
            testRuntimeOnly("org.junit.platform:junit-platform-launcher")

            implementation("org.springframework.boot:spring-boot-starter-data-jpa")
            implementation("org.springframework.boot:spring-boot-starter-web")

            implementation("org.springframework.grpc:spring-grpc-spring-boot-starter:${Version.GRPC}")
            implementation("org.springframework.grpc:spring-grpc-client-spring-boot-starter:${Version.GRPC}")
            implementation("org.springframework.grpc:spring-grpc-server-spring-boot-starter:${Version.GRPC}")
            implementation("io.grpc:grpc-services")

            testImplementation("org.springframework.boot:spring-boot-starter-test")
            testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
            testImplementation("io.kotest:kotest-runner-junit5:${Version.KOTEST}")
            testImplementation("io.kotest:kotest-assertions-core:${Version.KOTEST}")
            testImplementation("io.mockk:mockk:1.14.9")

            implementation("org.postgresql:postgresql:42.7.10")
            implementation("org.redisson:redisson:3.45.1")
        }

        kotlin {
            compilerOptions {
                freeCompilerArgs.addAll("-Xjsr305=strict")
            }
        }
    } else {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "com.google.protobuf")

        dependencies {
            api("io.grpc:grpc-stub:1.72.0")
            api("io.grpc:grpc-protobuf:1.72.0")
            api("io.grpc:grpc-kotlin-stub:1.4.3")
            api("com.google.protobuf:protobuf-kotlin:4.30.2")
        }

        protobuf {
            protoc { artifact = "com.google.protobuf:protoc:4.30.2" }
            plugins {
                id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.72.0" }
                id("grpckt") { artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.3:jdk8@jar" }
            }
            generateProtoTasks {
                all().forEach {
                    it.plugins {
                        id("grpc")
                        id("grpckt")
                    }
                }
            }
        }
    }
}

//protobuf {
//    protoc {
//        artifact = "com.google.protobuf:protoc"
//    }
//
//    plugins {
//        id("grpc") {
//            artifact = "io.grpc:protoc-gen-grpc-java"
//        }
//    }
//
//    generateProtoTasks {
//        all().forEach {
//            it.plugins {
//                id("grpc") {
//                    option("@generated=omit")
//                }
//            }
//        }
//    }
//}