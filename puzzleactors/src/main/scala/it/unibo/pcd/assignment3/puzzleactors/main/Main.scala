package it.unibo.pcd.assignment3.puzzleactors.main

import it.unibo.pcd.assignment3.puzzleactors.controller.Controller
import it.unibo.pcd.assignment3.puzzleactors.view.View
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.discard
import javafx.application.Application
import javafx.stage.Stage

import scala.jdk.CollectionConverters.MapHasAsScala

final class Main extends Application {

  override def start(primaryStage: Stage): Unit = {
    val rows: Int = 3
    val columns: Int = 5
    val parameters: Map[String, String] = getParameters.getNamed.asScala.toMap
    val host: String = parameters.getOrElse("h", "localhost")
    val port: Int = parameters.get("p").map(_.toInt).getOrElse(0)
    discard {
      View(
        primaryStage,
        rows,
        columns,
        ClassLoader.getSystemResource("bletchley-park-mansion.jpg").toExternalForm
      )(
        if (parameters.contains("H"))
          Controller(_)(host, port, parameters("H"), parameters.get("P").map(_.toInt).getOrElse(0))
        else
          Controller(rows, columns, _)(host, port)
      )
    }
  }
}

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}
