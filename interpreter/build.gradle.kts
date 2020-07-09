plugins {
    application
    kotlin("jvm") version "1.3.72"
}

repositories {
    mavenCentral()
    maven { setUrl("https://dl.bintray.com/hotkeytlt/maven") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.0")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.1.1") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.1") // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property-jvm:4.1.1") // for kotest property test
}

version = "0.1"
group = "com.github.kright.interpreter"

application {
    mainClassName = "com.github.kright.interpreter.ParserKt"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
