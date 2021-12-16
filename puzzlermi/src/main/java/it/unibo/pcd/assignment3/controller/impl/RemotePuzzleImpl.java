package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.model.PuzzleBoard;
import it.unibo.pcd.assignment3.model.Tile;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class RemotePuzzleImpl implements RemotePuzzle {
    private final PuzzleBoard board;
    private final BiConsumer<Tile, Tile> listener;

    public RemotePuzzleImpl(final PuzzleBoard board, final BiConsumer<Tile, Tile> listener) {
        this.board = Objects.requireNonNull(board);
        this.listener = Objects.requireNonNull(listener);
    }

    @Override
    public void swap(final Tile firstTile, final Tile secondTile) {
        this.listener.accept(firstTile, secondTile);
    }

    @Override
    public List<Tile> getTiles() {
        return this.board.getTiles();
    }
}
