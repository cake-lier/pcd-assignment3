package it.unibo.pcd.assignment3.actors.main

import it.unibo.pcd.assignment3.actors.view.View
import it.unibo.pcd.assignment3.actors.AnyOps.discard
import javafx.application.Application
import javafx.stage.Stage

/** The main class for the application with a graphical user interface for the user. */
final class Main extends Application {

  override def start(primaryStage: Stage): Unit = discard(View(primaryStage))
}

/** Companion object for the [[Main]] class, executing the code for launching it. */
object Main extends App {
  Application.launch(classOf[Main], args: _*)
}
