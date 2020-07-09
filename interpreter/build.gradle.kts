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
}

version = "0.1"
group = "com.github.kright.interpreter"

application {
    mainClassName = "com.github.kright.interpreter.ParserKt"
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
