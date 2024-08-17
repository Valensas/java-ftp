plugins {
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
	id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
	id("maven-publish")
}

group = "com.valensas"
version = "0.1.0"

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	mavenLocal()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Ftp
	implementation("commons-net:commons-net:3.11.1")
	implementation("com.jcraft:jsch:0.1.55")
	implementation("org.apache.sshd:sshd-core:2.11.0")
	implementation("org.apache.sshd:sshd-common:2.11.0")
	implementation("org.apache.sshd:sshd-sftp:2.11.0")
	implementation("org.apache.ftpserver:ftpserver-core:1.0.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}
}

signing {
	val keyId = System.getenv("SIGNING_KEYID")
	val secretKey = System.getenv("SIGNING_SECRETKEY")
	val passphrase = System.getenv("SIGNING_PASSPHRASE")

	useInMemoryPgpKeys(keyId, secretKey, passphrase)
}

centralPortal {
	username = System.getenv("SONATYPE_USERNAME")
	password = System.getenv("SONATYPE_PASSWORD")

	pom {
		name = "Java Ftp"
		description = "This library contains embedded ftp server and ftp factory which supports ftp, ftps, sftp."
		url = "https://valensas.com/"
		scm {
			url = "https://github.com/Valensas/java-ftp"
		}

		licenses {
			license {
				name.set("The Apache License, Version 2.0")
				url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
			}
		}

		developers {
			developer {
				id.set("0")
				name.set("Valensas")
				email.set("info@valensas.com")
			}
		}
	}
}
