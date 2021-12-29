package it.unibo.pcd.assignment3.puzzleactors.view

import it.unibo.pcd.assignment3.puzzleactors.controller._
import it.unibo.pcd.assignment3.puzzleactors.model.{Position, Tile}
import javafx.application.Platform
import javafx.scene.image.{Image, WritableImage}
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.stage.Stage

trait View {

  def displayTiles(tiles: Seq[Tile]): Unit

  def displaySolution(): Unit
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
    private val board: BorderPane = new BorderPane
    board.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    private val grid: GridPane = new GridPane
    board.setCenter(grid)
    private val image: Image = new Image(imageUrl)
    private val tileWidth = image.getWidth.toInt / columns
    private val tileHeight = image.getHeight.toInt / rows
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
    primaryStage.setScene(new Scene(board))
    primaryStage.show()

    private def displayTilesImmediately(tiles: Seq[Tile]): Unit = {
      selectionManager.clearSelection()
      grid.getChildren.removeAll(grid.getChildren)
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
      grid.layout()
    }

    override def displayTiles(tiles: Seq[Tile]): Unit = Platform.runLater(() => displayTilesImmediately(tiles))

    override def displaySolution(): Unit =
      Platform.runLater(() => new Alert(Alert.AlertType.INFORMATION, "Puzzle Completed!").showAndWait())
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
