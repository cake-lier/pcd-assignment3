package it.unibo.pcd.assignment3.puzzlermi.model.impl;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.PuzzleBoard;
import it.unibo.pcd.assignment3.puzzlermi.model.Tile;

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

    public PuzzleBoardImpl(final List<Tile> tiles) {
        this.tiles = new ArrayList<>(tiles);
    }

    @Override
    public List<Tile> getTiles() {
        return List.copyOf(this.tiles);
    }

    @Override
    public void swap(final Position firstPosition, final Position secondPosition) {
        this.tiles
            .stream()
            .filter(t -> t.getCurrentPosition().equals(firstPosition))
            .findFirst()
            .ifPresent(t1 -> this.tiles
                                 .stream()
                                 .filter(t -> t.getCurrentPosition().equals(secondPosition))
                                 .findFirst()
                                 .ifPresent(t2 -> {
                                    this.tiles.remove(t1);
                                    this.tiles.remove(t2);
                                    this.tiles.add(new TileImpl(t1.getOriginalPosition(), secondPosition));
                                    this.tiles.add(new TileImpl(t2.getOriginalPosition(), firstPosition));
                                 }));
	}

    @Override
    public boolean isSolution() {
    	return this.tiles.stream().allMatch(Tile::isInRightPlace);
    }
}
