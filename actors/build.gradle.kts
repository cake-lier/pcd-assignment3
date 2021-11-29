plugins {
    scala
    application
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
