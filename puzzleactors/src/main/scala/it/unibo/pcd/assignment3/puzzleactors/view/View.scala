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

/** The View component of this application. It should capture user input and be notified of changes into the Model component which
  * should appear to the user.
  *
  * A new instance must be constructed through its companion object.
  */
trait View {

  /** Shows a new arrangement of [[Tile]]s into the grid-like view which displays the puzzle. The [[Tile]]s are arranged following
    * their current position.
    * @param tiles
    *   the new arrangement of [[Tile]]s to be displayed
    */
  def displayTiles(tiles: Seq[Tile]): Unit

  /** Displays the message to the player indicating the completion of the game. */
  def displaySolution(): Unit

  /** Displays an error message to the player when they cannot join a game session due to an error. */
  def displayJoinError(): Unit
}

/** Companion object to the View trait, containing its factory method. */
object View {

  /* An implementation of the View trait which creates a Graphical User Interface using JavaFX. */
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

  /** The factory method for creating a new instance of the [[View]] trait given the [[Stage]] on which displaying it, the
    * dimensions and the image of the puzzle to display and the factory for creating the Controller component from this View.
    * @param primaryStage
    *   the stage created by JavaFX on which displaying the puzzle view
    * @param rows
    *   the number of rows of the grid-like view displaying the puzzle
    * @param columns
    *   the number of columns of the grid-like view displaying the puzzle
    * @param imageUrl
    *   the URL of the image to be used in the puzzle
    * @param controllerFactory
    *   the factory capable of creating a new [[Controller]] given a view instance
    * @return
    *   a new instance of the [[View]] trait
    */
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
