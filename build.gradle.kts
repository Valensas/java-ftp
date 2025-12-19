import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("jacoco")
    id("org.jmailen.kotlinter") version "4.4.1"
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
}

group = "com.valensas"
version = "0.2.23"

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    if (project.hasProperty("GITLAB_REPO_URL")) {
        maven {
            name = "Gitlab"
            url = uri(project.property("GITLAB_REPO_URL").toString())
            credentials(HttpHeaderCredentials::class.java) {
                name = project.findProperty("GITLAB_TOKEN_NAME")?.toString()
                value = project.findProperty("GITLAB_TOKEN")?.toString()
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
    mavenLocal()
}

tasks.getByName<Jar>("jar") {
    archiveClassifier.set("")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.valensas:graalvm-native-support:1.0.4")

    // Ftp
    implementation("commons-net:commons-net:3.12.0")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("org.apache.sshd:sshd-core:2.16.0")
    implementation("org.apache.sshd:sshd-common:2.16.0")
    implementation("org.apache.sshd:sshd-sftp:2.16.0")
    implementation("org.apache.ftpserver:ftpserver-core:1.2.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.98".toBigDecimal()
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

