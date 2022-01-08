package it.unibo.pcd.assignment3.puzzlermi.view.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.Controller;
import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.Tile;
import it.unibo.pcd.assignment3.puzzlermi.model.impl.PositionImpl;
import it.unibo.pcd.assignment3.puzzlermi.view.SelectionManager;
import it.unibo.pcd.assignment3.puzzlermi.view.View;
import javafx.application.Platform;
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
import org.apache.commons.lang3.function.FailableFunction;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ViewImpl implements View {
    private final Controller controller;
	private final SelectionManager selectionManager;
    private final Map<Position, Image> tilesImages;
    private final Stage primaryStage;

    public ViewImpl(final Stage primaryStage,
                    final int rows,
                    final int columns,
                    final String imageUrl,
                    final FailableFunction<View, Controller, Exception> controllerFactory)
        throws Exception {
        primaryStage.setTitle("Puzzle");
    	primaryStage.setResizable(false);
        final var board = new BorderPane();
        board.setBorder(new Border(new BorderStroke(
            Color.GRAY,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            BorderWidths.DEFAULT
        )));
        this.primaryStage = Objects.requireNonNull(primaryStage);
        final var image = new Image(imageUrl);
        final var tileWidth = (int) image.getWidth() / columns;
        final var tileHeight = (int) image.getHeight() / rows;
        this.tilesImages = IntStream.range(0, columns)
                                    .boxed()
                                    .flatMap(i -> IntStream.range(0, rows)
                                                     .mapToObj(j -> Map.<PositionImpl, Image>entry(
                                                         new PositionImpl(i, j),
                                                         new WritableImage(
                                                             image.getPixelReader(),
                                                             i * tileWidth,
                                                             j * tileHeight,
                                                             tileWidth,
                                                             tileHeight
                                                         )
                                                     )))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.controller = controllerFactory.apply(this);
        this.selectionManager = new SelectionManagerImpl(this.controller);
        primaryStage.setOnCloseRequest(e -> this.controller.exit());
    }

    @Override
    public void displayTiles(final List<Tile> tiles) {
        Platform.runLater(() -> {
            this.selectionManager.clearSelection();
            final var board = new BorderPane();
            final var grid = new GridPane();
            board.setCenter(grid);
            tiles.forEach(t -> grid.add(
                new TileButton(
                    this.tilesImages.get(t.getOriginalPosition()),
                    () -> this.selectionManager.selectPosition(t.getCurrentPosition())
                ),
                t.getCurrentPosition().getX(),
                t.getCurrentPosition().getY()
            ));
            this.primaryStage.setScene(new Scene(board));
            this.primaryStage.show();
        });
    }

    @Override
    public void displaySolution() {
        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Puzzle Completed!").showAndWait());
    }
}
