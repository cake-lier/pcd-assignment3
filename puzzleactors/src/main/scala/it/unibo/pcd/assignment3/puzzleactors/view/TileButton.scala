package it.unibo.pcd.assignment3.puzzleactors.view

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{Border, BorderStroke, BorderStrokeStyle, BorderWidths, CornerRadii}
import javafx.scene.paint.Color

/** A [[Button]] used to represent a [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]] of the puzzle in the grid-like view that
  * displays it, which can be pressed for selecting that [[it.unibo.pcd.assignment3.puzzleactors.model.Tile]].
  * @param image
  *   the part of the puzzle image that this button should display
  * @param onButtonClicked
  *   the handler to be executed when this button is pressed with a mouse click
  */
class TileButton(image: Image, onButtonClicked: () => Unit) extends Button {
  setPadding(Insets.EMPTY)
  setGraphic(new ImageView(image))
  setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
  setOnAction(_ => {
    setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    onButtonClicked()
  })
}
