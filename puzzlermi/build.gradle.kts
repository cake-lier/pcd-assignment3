plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

application {
    mainClass.set("it.unibo.pcd.assignment3.puzzlermi.main.PuzzleGameApplication")
}

javafx {
    version = "17"
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
}
