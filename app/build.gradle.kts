plugins {
    kotlin("jvm") version "1.5.21"
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "app"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    //MQTT CONNECTION
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:_")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:_")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:_")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    /*
     * Dependency Used By App
     */

    // For Postgres DB Connections
    implementation("com.impossibl.pgjdbc-ng:pgjdbc-ng:_")
    implementation("org.jetbrains.exposed:exposed-jdbc:_")
    implementation("org.jetbrains.exposed:exposed-jodatime:_")
    implementation("com.zaxxer:HikariCP:_")

    // Logs
    implementation("org.slf4j:slf4j-simple:_")
    implementation("io.github.microutils:kotlin-logging:_")

    // JSON Object Format
    implementation("org.json:json:_")

    // Environment Variable
    implementation("io.github.cdimascio:java-dotenv:_")

    // Datetime Format
    implementation("joda-time:joda-time:_")

    // HTTP Request
    implementation("org.http4k:http4k-client-apache:_")

    // Route / Endpoint
    implementation("io.javalin:javalin:_")

    // Monggo
    implementation("org.litote.kmongo:kmongo:4.4.0")

    //Cron
    implementation("org.quartz-scheduler:quartz:2.3.0")

    // Bouncy Castles
    implementation ("org.bouncycastle:bcpkix-jdk15on:_")

    // Java 17 compatibility
    implementation(group = "org.springframework.boot", name = "spring-boot-starter", version = "2.5.4")
    implementation(group = "org.springframework.boot", name = "spring-boot-starter-test", version = "2.5.4")

    // JWT ENCRYPT
    implementation("io.jsonwebtoken:jjwt:0.9.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    if (name == "run") {
        jvmArgs = listOf("--enable-preview")
    }
}