package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.model.Position;
import it.unibo.pcd.assignment3.model.PuzzleBoard;
import it.unibo.pcd.assignment3.model.Tile;
import it.unibo.pcd.assignment3.view.View;

import java.util.List;
import java.util.Objects;

public class RemotePuzzleImpl implements RemotePuzzle {
    private final PuzzleBoard board;
    private final View view;

    public RemotePuzzleImpl(final PuzzleBoard board, final View view) {
        this.board = Objects.requireNonNull(board);
        this.view = Objects.requireNonNull(view);
    }

    @Override
    public void swap(final Position firstPosition, final Position secondPosition) {
        this.board.swap(firstPosition, secondPosition);
        this.view.displayTiles(this.board.getTiles());
        if (this.board.isSolution()) {
            this.view.displaySolution();
        }
    }

    @Override
    public List<Tile> getTiles() {
        return this.board.getTiles();
    }
}
