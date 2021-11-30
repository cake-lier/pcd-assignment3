package it.unibo.pcd.assignment3.main

import it.unibo.pcd.assignment3.view.View
import it.unibo.pcd.assignment3.AnyOps.discard
import javafx.application.Application
import javafx.application.Application.launch
import javafx.stage.Stage

/** The main class for the application with a graphical user interface for the user. */
final class Main extends Application {

  override def start(primaryStage: Stage): Unit = discard(View(primaryStage))
}

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}
