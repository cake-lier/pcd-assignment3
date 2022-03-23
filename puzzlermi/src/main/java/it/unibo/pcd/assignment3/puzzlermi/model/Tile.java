package it.unibo.pcd.assignment3.puzzlermi.model;

/**
 * A piece of a puzzle that can be swapped with another to reorder and solve it. It can be kept in order along with others using
 * their current {@link Position}s to display the current tiles' arrangement.
 */
public interface Tile extends Comparable<Tile> {

    /**
     * Returns the original {@link Position} of this tile, the one in the reordered puzzle.
     * @return the {@link Position} in the reordered puzzle
     */
    Position getOriginalPosition();

    /**
     * Returns the current {@link Position} of this tile, the one in the current tiles' arrangement.
     * @return the {@link Position} in the current tiles' arrangement
     */
    Position getCurrentPosition();

    /**
     * Returns whether this tile is currently in the same {@link Position} as its one in the reordered puzzle.
     * @return whether this tile is currently in the same {@link Position} as its one in the reordered puzzle
     */
    boolean isInRightPlace();
}
