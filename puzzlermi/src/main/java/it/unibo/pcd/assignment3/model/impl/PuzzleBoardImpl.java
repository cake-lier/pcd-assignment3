package it.unibo.pcd.assignment3.model.impl;

import it.unibo.pcd.assignment3.model.PuzzleBoard;
import it.unibo.pcd.assignment3.model.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PuzzleBoardImpl implements PuzzleBoard {
    private final List<Tile> tiles;

    public PuzzleBoardImpl(final int rows, final int columns) {
        final var randomPositions =
            IntStream.range(0, columns)
                     .boxed()
                     .flatMap(i -> IntStream.range(0, rows).mapToObj(j -> new PositionImpl(i, j)))
                     .collect(Collectors.toList());
        Collections.shuffle(randomPositions);
        this.tiles = IntStream.range(0, rows * columns)
                              .<Tile>mapToObj(
                                  p -> new TileImpl(randomPositions.get(p), new PositionImpl(p % columns, p / columns))
                              )
                              .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Tile> getTiles() {
        return Collections.unmodifiableList(this.tiles);
    }

    @Override
    public void swap(final Tile firstTile, final Tile secondTile) {
        this.tiles.remove(firstTile);
        this.tiles.remove(secondTile);
        this.tiles.add(new TileImpl(firstTile.getOriginalPosition(), secondTile.getCurrentPosition()));
        this.tiles.add(new TileImpl(secondTile.getOriginalPosition(), firstTile.getCurrentPosition()));
	}

    @Override
    public boolean isSolution() {
    	return this.tiles.stream().allMatch(Tile::isInRightPlace);
    }
}
