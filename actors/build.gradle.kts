plugins {
    scala
    application
    id("cz.augi.gradle.wartremover") version "0.14.2"
    id("cz.alenkacz.gradle.scalafmt") version "1.16.2"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

repositories {
    mavenCentral()
}

val scalaVersion = "2.13"
val scalaCompleteVersion = "$scalaVersion.7"

dependencies {
    implementation("org.scala-lang:scala-library:$scalaCompleteVersion")
    implementation("com.typesafe.akka:akka-actor-typed_$scalaVersion:2.6.17")
    implementation("com.typesafe.akka:akka-slf4j_$scalaVersion:2.6.17")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation(fileTree("lib").include("**/*.jar"))
}

application {
    mainClass.set("it.unibo.pcd.assignment3.main.Main")
}

wartremover {
    errorWarts.addAll(warningWarts)
    errorWarts.addAll(setOf(
        "ArrayEquals",
        "AnyVal",
        "Equals",
        "ExplicitImplicitTypes",
        "FinalCaseClass",
        "FinalVal",
        "JavaConversions",
        "JavaSerializable",
        "LeakingSealed",
        "MutableDataStructures",
        "Nothing",
        "Null",
        "PublicInference"
    ))
    errorWarts.removeAll(setOf("DefaultArguments", "Var"))
    warningWarts.clear()
    excludedFiles.addAll(fileTree("src/main/scala/it/unibo/pcd/assignment3/view").map { it.path }.asSequence())
}

scalafmt {
    configFilePath = ".scalafmt.conf"
}

javafx {
    version = "17"
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
}
