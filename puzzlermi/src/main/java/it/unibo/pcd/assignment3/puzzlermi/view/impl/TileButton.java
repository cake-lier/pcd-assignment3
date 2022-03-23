package it.unibo.pcd.assignment3.puzzlermi.view.impl;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * A {@link Button} used to represent a {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile} of the puzzle in the grid-like view
 * that displays it, which can be pressed for selecting that {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile}.
 */
public class TileButton extends Button {

	/**
	 * Default constructor.
	 * @param image the part of the puzzle image that this button should display
	 * @param onButtonClicked the handler to be executed when this button is pressed with a mouse click
	 */
	public TileButton(final Image image, final Runnable onButtonClicked) {
		super();
        this.setPadding(Insets.EMPTY);
		this.setGraphic(new ImageView(image));
        this.setBorder(new Border(new BorderStroke(
            Color.GRAY,
			BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY,
			BorderWidths.DEFAULT
        )));
		this.setOnAction(e -> {
            this.setBorder(new Border(new BorderStroke(
                Color.RED,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                BorderWidths.DEFAULT
            )));
            onButtonClicked.run();
        });
	}
}
