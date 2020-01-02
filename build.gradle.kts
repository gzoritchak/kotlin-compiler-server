import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by System.getProperties()

group = "com.compiler.server"
version = "compiler-server-$kotlinVersion-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val kotlinDependency: Configuration by configurations.creating
val kotlinJsDependency: Configuration by configurations.creating
val libJSFolder = "$kotlinVersion-js"

val copyDependencies by tasks.creating(Copy::class) {
    from(kotlinDependency)
    into(kotlinVersion)
}
val copyJSDependencies by tasks.creating(Copy::class) {
    from(files(Callable { kotlinJsDependency.map { zipTree(it)} }))
    into(libJSFolder)
}

plugins {
    id("org.springframework.boot") version "2.2.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.60"
    kotlin("plugin.spring") version "1.3.50"
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://kotlin.bintray.com/kotlin-plugin")
    }
    afterEvaluate {
        dependencies {
            implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.10")
        }
    }
}

rootDir.resolve("src/main/resources/libraries.properties").apply{
    println(absolutePath)
    parentFile.mkdirs()
    writeText("""
        kotlin.version=${kotlinVersion}
        libraries.folder.jvm=${kotlinVersion}
        libraries.folder.js=${libJSFolder}
    """.trimIndent())
}

dependencies {

    kotlinDependency("junit:junit:4.12")
    kotlinDependency("org.hamcrest:hamcrest-core:1.3")
    kotlinDependency("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.10")
    kotlinDependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    kotlinDependency("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    kotlinDependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
    kotlinJsDependency("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("junit:junit:4.12")
    implementation("org.jetbrains.intellij.deps:trove4j:1.0.20181211")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-plugin-ij193:$kotlinVersion") {
        isTransitive = false
    }
    implementation(project(":executors"))

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xskip-metadata-version-check")
        jvmTarget = "1.8"
    }
    dependsOn(copyDependencies)
    dependsOn(copyJSDependencies)
    dependsOn(":executors:jar")
}

tasks.withType<Test> {
    useJUnitPlatform()
}