plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
}

repositories {
    mavenCentral()
}

dependencies {
}

application {
    mainClass.set("it.unibo.pcd.assignment3.main.PuzzleGameApplication")
}

javafx {
    version = "17"
    modules("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
}
