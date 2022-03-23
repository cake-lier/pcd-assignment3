package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.PuzzleBoard;
import it.unibo.pcd.assignment3.puzzlermi.model.Tile;
import it.unibo.pcd.assignment3.puzzlermi.view.View;

import java.util.List;
import java.util.Objects;

/**
 * An implementation of the {@link RemotePuzzle} interface.
 */
public class RemotePuzzleImpl implements RemotePuzzle {
    private final PuzzleBoard board;
    private final View view;

    /**
     * Default constructor.
     * @param board the {@link PuzzleBoard} containing the puzzle to be accessed through this remote object
     * @param view the {@link View} component of this application
     */
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.board.equals(((RemotePuzzleImpl) o).board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.board);
    }

    @Override
    public String toString() {
        return "RemotePuzzleImpl{board=" + this.board + "}";
    }
}
