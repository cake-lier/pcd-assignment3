package it.unibo.pcd.assignment3.puzzlermi.model;

import java.util.List;

/**
 * The board of a puzzle, containing all of its {@link Tile}s in the current arrangement.
 */
public interface PuzzleBoard {

    /**
     * Returns all the {@link Tile}s that make up the puzzle, ordered from the top left to the bottom right one following the
     * current arrangement.
     * @return all the {@link Tile}s that make up the puzzle
     */
    List<Tile> getTiles();

    /**
     * Swaps the two {@link Tile}s corresponding to the two {@link Position}s given.
     * @param firstPosition the {@link Position} of the first {@link Tile} to swap
     * @param secondPosition the {@link Position} of the second {@link Tile} to swap
     */
    void swap(Position firstPosition, Position secondPosition);

    /**
     * Returns whether the current arrangement of tiles constitutes a solution to the puzzle.
     * @return whether the current arrangement of tiles constitutes a solution to the puzzle
     */
    boolean isSolution();
}
