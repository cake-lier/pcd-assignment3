package it.unibo.pcd.assignment3.puzzleactors.view

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{Border, BorderStroke, BorderStrokeStyle, BorderWidths, CornerRadii}
import javafx.scene.paint.Color

class TileButton(image: Image, onButtonClicked: () => Unit) extends Button {
  setPadding(Insets.EMPTY)
  setGraphic(new ImageView(image))
  setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
  setOnAction(_ => {
    setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)))
    onButtonClicked()
  })
}
