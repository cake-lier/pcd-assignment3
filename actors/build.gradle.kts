plugins {
    scala
    application
    id("cz.augi.gradle.wartremover") version "0.14.2"
    id("cz.alenkacz.gradle.scalafmt") version "1.16.2"
}

repositories {
    mavenCentral()
}

val scalaVersion = "2.13"

dependencies {
    implementation("org.scala-lang:scala-library:$scalaVersion.7")
    implementation("com.typesafe.akka:akka-actor-typed_$scalaVersion:2.6.17")
    implementation(fileTree("lib").include("**/*.jar"))
}

application {
    //mainClass.set("it.unibo.pcd.assignment3.Main")
}

wartremover {
    errorWarts.addAll(warningWarts)
    errorWarts.addAll(setOf(
        "ArrayEquals",
        "AnyVal",
        "Enumeration",
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
        "PublicInference",
        "Recursion",
        "While"
    ))
    errorWarts.removeAll(setOf("DefaultArguments", "Null", "Var"))
}

scalafmt {
    configFilePath = ".scalafmt.conf"
}
