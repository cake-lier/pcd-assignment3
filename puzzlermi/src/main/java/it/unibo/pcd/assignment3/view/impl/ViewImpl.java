package it.unibo.pcd.assignment3.view.impl;

import it.unibo.pcd.assignment3.controller.Controller;
import it.unibo.pcd.assignment3.model.Position;
import it.unibo.pcd.assignment3.model.Tile;
import it.unibo.pcd.assignment3.model.impl.PositionImpl;
import it.unibo.pcd.assignment3.view.SelectionManager;
import it.unibo.pcd.assignment3.view.View;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ViewImpl implements View {
    private final Controller controller;
	private final SelectionManager selectionManager;
    private final Map<Position, Image> tiles;

    public ViewImpl(final Stage primaryStage,
                    final String imageUrl,
                    final Function<View, Controller> controllerFactory) {
        this.controller = controllerFactory.apply(this);
        this.selectionManager = new SelectionManagerImpl(this.controller);
        primaryStage.setTitle("Puzzle");
    	primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> this.controller.exit());
        final var board = new BorderPane();
        board.setBorder(new Border(new BorderStroke(
            Color.GRAY,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT
        )));
        final var grid = new GridPane();
        board.setCenter(grid);
        final var image = new Image(imageUrl);
        final var tileWidth = (int) image.getWidth() / columns;
        final var tileHeight = (int) image.getHeight() / rows;
        this.tiles = IntStream.range(0, columns).boxed().flatMap(i -> IntStream.range(0, rows).mapToObj(j -> {
                                        final var reader = image.getPixelReader();
                                        final var newImage = new WritableImage(
                                            reader,
                                            i * tileWidth,
                                            j * tileHeight,
                                            tileWidth,
                                            tileHeight
                                        );
                                        return Map.<PositionImpl, Image>entry(new PositionImpl(i, j), newImage);
                                    }))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        paintPuzzle(grid);
        primaryStage.setScene(new Scene(board));
        primaryStage.show();
    }
    
    private void paintPuzzle(final GridPane grid) {
    	grid.getChildren().removeAll(grid.getChildren());
    	this.controller.getTiles().forEach(t -> {
    		final var button = new TileButton(
                this.tiles.get(t.getOriginalPosition()),
                () -> this.selectionManager.selectTile(t, () -> {
            		paintPuzzle(grid);
                	checkSolution();
            	})
            );
            grid.add(button, t.getCurrentPosition().getX(), t.getCurrentPosition().getY());
    	});
        grid.layout();
    }

    private void checkSolution() {
    	if (this.controller.isSolution()) {
    		new Alert(Alert.AlertType.INFORMATION, "Puzzle Completed!").showAndWait();
    	}
    }

    @Override
    public void displayTiles(final List<Tile> tiles) {

    }

    @Override
    public void displaySolution() {

    }
}
