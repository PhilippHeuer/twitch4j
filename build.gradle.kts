// Plugins
plugins {
	signing
	`java-library`
	`maven-publish`
	id("io.freefair.lombok") version "5.3.3.3"
	id("com.coditory.manifest") version "0.1.14"
	id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = group
version = version

// All-Projects
allprojects {
	// Repositories
	repositories {
		mavenCentral()
	}

	tasks {
		// disable 'lombok.config' generation
		withType<io.freefair.gradle.plugins.lombok.tasks.GenerateLombokConfig> {
			enabled = false
		}
	}
}

// Subprojects
subprojects {
	apply(plugin = "signing")
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")
	apply(plugin = "io.freefair.lombok")
	apply(plugin = "com.coditory.manifest")
	apply(plugin = "com.github.johnrengelman.shadow")

	base {
		archivesBaseName = artifactId
	}

	lombok {
		version.set("1.18.20")
	}

	// Source Compatibility
	java {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
		withSourcesJar()
		withJavadocJar()
	}

	// Dependency Management for Subprojects
	dependencies {
		constraints {
			// Annotations
			api(group = "org.jetbrains", name = "annotations", version = "21.0.1")

			// Caching
			api(group = "com.github.ben-manes.caffeine", name = "caffeine", version = "2.9.2")

			// Apache Commons
			api(group = "commons-configuration", name = "commons-configuration", version = "1.10")

			// Rate Limiting
			api(group = "com.github.vladimir-bukhtoyarov", name = "bucket4j-core", version = "4.7.0")

			// HTTP
			api(group = "com.squareup.okhttp3", name = "okhttp", version = "4.9.1")

			// Event Dispatcher
			api(group = "com.github.philippheuer.events4j", name = "events4j-core", version = "0.9.8")
			api(group = "com.github.philippheuer.events4j", name = "events4j-handler-simple", version = "0.9.8")

			// Credential Manager
			api(group = "com.github.philippheuer.credentialmanager", name = "credentialmanager", version = "0.1.2")

			// HTTP Client
			api(group = "io.github.openfeign", name = "feign-slf4j", version = "11.2")
			api(group = "io.github.openfeign", name = "feign-okhttp", version = "11.2")
			api(group = "io.github.openfeign", name = "feign-jackson", version = "11.2")
			api(group = "io.github.openfeign", name = "feign-hystrix", version = "11.2")

			// WebSocket
			api(group = "com.neovisionaries", name = "nv-websocket-client", version = "2.14")

			// Hystrix
			api(group = "com.netflix.hystrix", name = "hystrix-core", version = "1.5.18")
		}

		// Apache Commons
		api(group = "commons-io", name = "commons-io", version = "2.9.0")
		api(group = "org.apache.commons", name = "commons-lang3", version = "3.12.0")

		// Logging
		api(group = "org.slf4j", name = "slf4j-api", version = "1.7.30")

		// Jackson BOM
		implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.3"))

		// Test
		testImplementation(platform("org.junit:junit-bom:5.7.2"))
		testImplementation(group = "org.junit.jupiter", name = "junit-jupiter")
		testImplementation(group = "ch.qos.logback", name = "logback-classic", version = "1.2.3")
	}

	publishing {
		repositories {
			maven {
				name = "maven"
				url = uri(project.mavenRepositoryUrl)
				credentials {
					username = project.mavenRepositoryUsername
					password = project.mavenRepositoryPassword
				}
			}
		}
		publications {
			create<MavenPublication>("main") {
				from(components["java"])
				artifactId = project.artifactId
				pom.default()
			}
		}
	}

	signing {
		useGpgCmd()
		sign(publishing.publications["main"])
	}

	// Source encoding
	tasks {
		// javadoc / html5 support
		withType<Javadoc> {
			// hide javadoc warnings (a lot from delombok)
			(options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")

			if (JavaVersion.current().isJava9Compatible) {
				(options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
			}
		}

		// shadowjar & relocation
		val relocateShadowJar by creating(com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation::class) {
			target = shadowJar.get()
			prefix = "com.github.twitch4j.shaded.${"$version".replace(".", "_")}"
		}

		// jar artifact id and version
		withType<Jar> {
			if (this is com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar) {
				dependsOn(relocateShadowJar)
				archiveClassifier.set("shaded")
			}
			dependsOn(project.tasks.manifest)
			manifest.from(File(buildDir, "resources/main/META-INF/MANIFEST.MF"))
		}

		// compile options
		withType<JavaCompile> {
			options.encoding = "UTF-8"
		}

		// javadoc & delombok
		val delombok by getting(io.freefair.gradle.plugins.lombok.tasks.Delombok::class)
		javadoc {
			dependsOn(delombok)
			source(delombok)
			options {
				title = "${project.artifactId} (v${project.version})"
				windowTitle = "${project.artifactId} (v${project.version})"
				encoding = "UTF-8"
			}
		}

		// test
		test {
			useJUnitPlatform {
				includeTags("unittest")
				excludeTags("integration")
			}
		}
	}
}

tasks.register<Javadoc>("aggregateJavadoc") {
	group = JavaBasePlugin.DOCUMENTATION_GROUP
	options {
		title = "${rootProject.name} (v${project.version})"
		windowTitle = "${rootProject.name} (v${project.version})"
		encoding = "UTF-8"
	}

	source(subprojects.map { it.tasks.delombok.get() })
	classpath = files(subprojects.map { it.sourceSets["main"].compileClasspath })

	setDestinationDir(file("${rootDir}/docs/static/javadoc"))
}
