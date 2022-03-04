package it.unibo.pcd.assignment3.puzzleactors.view

import it.unibo.pcd.assignment3.puzzleactors.controller._
import it.unibo.pcd.assignment3.puzzleactors.model.{Position, Tile}
import it.unibo.pcd.assignment3.puzzleactors.AnyOps.discard
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.image.{Image, WritableImage}
import javafx.scene.layout.{Border, BorderPane, BorderStroke, BorderStrokeStyle, BorderWidths, CornerRadii, GridPane}
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage

trait View {

  def displayTiles(tiles: Seq[Tile]): Unit

  def displaySolution(): Unit

  def displayJoinError(): Unit
}

object View {

  private class ViewImpl(
    primaryStage: Stage,
    rows: Int,
    columns: Int,
    imageUrl: String,
    controllerFactory: View => Controller
  ) extends View {

    primaryStage.setTitle("Puzzle")
    primaryStage.setResizable(false)
    private val image: Image = new Image(imageUrl)
    private val tileWidth: Int = image.getWidth.toInt / columns
    private val tileHeight: Int = image.getHeight.toInt / rows
    private val tilesImages: Map[Position, Image] =
      (0 until columns)
        .flatMap(i =>
          (0 until rows)
            .map { j =>
              Position(i, j) -> new WritableImage(image.getPixelReader, i * tileWidth, j * tileHeight, tileWidth, tileHeight)
            }
        )
        .toMap
    private val controller: Controller = controllerFactory(this)
    private val selectionManager: SelectionManager = SelectionManager(controller)
    primaryStage.setOnCloseRequest(_ => controller.exit())

    override def displayTiles(tiles: Seq[Tile]): Unit = Platform.runLater(() => {
      selectionManager.clearSelection()
      val board: BorderPane = new BorderPane
      board.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
      val grid: GridPane = new GridPane
      board.setCenter(grid)
      discard {
        grid.getChildren.removeAll(grid.getChildren)
      }
      tiles.foreach { t =>
        grid.add(
          new TileButton(
            tilesImages(t.originalPosition),
            () => selectionManager.selectPosition(t.currentPosition)
          ),
          t.currentPosition.x,
          t.currentPosition.y
        )
      }
      val scene: Scene = new Scene(board)
      primaryStage.setScene(scene)
      primaryStage.show()
    })

    override def displaySolution(): Unit =
      Platform.runLater(() => discard(new Alert(Alert.AlertType.INFORMATION, "Puzzle Completed!").showAndWait()))

    override def displayJoinError(): Unit = {
      Platform.runLater(() =>
        discard {
          new Alert(Alert.AlertType.ERROR, "Impossible to join the game").showAndWait()
          controller.exit()
        }
      )
    }
  }

  def apply(
    primaryStage: Stage,
    rows: Int,
    columns: Int,
    imageUrl: String
  )(
    controllerFactory: View => Controller
  ): View =
    new ViewImpl(primaryStage, rows, columns, imageUrl, controllerFactory)
}
