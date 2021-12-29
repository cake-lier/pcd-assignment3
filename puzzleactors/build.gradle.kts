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

dependencies {
    implementation("org.scala-lang:scala-library:$scalaVersion.7")
    implementation("com.typesafe.akka:akka-actor-typed_$scalaVersion:2.6.17")
    implementation("com.typesafe.akka:akka-cluster-typed_$scalaVersion:2.6.17")
    implementation("com.typesafe.akka:akka-serialization-jackson_$scalaVersion:2.6.17")
    implementation("org.slf4j:slf4j-simple:1.6.1")
}

application {
    mainClass.set("it.unibo.pcd.assignment3.puzzleactors.main.Main")
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
        "ImplicitParameter",
        "JavaConversions",
        "JavaSerializable",
        "LeakingSealed",
        "MutableDataStructures",
        "Nothing",
        "Null",
        "PublicInference",
        "While"
    ))
    errorWarts.removeAll(setOf("DefaultArguments", "Var"))
    warningWarts.clear()
}

scalafmt {
    configFilePath = ".scalafmt.conf"
}

javafx {
    version = "17"
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
}
