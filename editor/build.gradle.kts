plugins {
    application
    kotlin("jvm") version "1.3.72"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

version = "0.1"
group = "com.github.kright.codeeditor"

application {
    mainClassName = "com.github.kright.editor.EditorKt"
}

task("prepareInterpreter") {
    dependsOn(":interpreter:distZip")
    doLast{
        copy {
            from(zipTree("../interpreter/build/distributions/interpreter-0.1.zip"))
            into("build/interpreter")
        }
    }
}

tasks.named("run"){
    dependsOn("prepareInterpreter")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}