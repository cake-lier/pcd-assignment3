package it.unibo.pcd.assignment3.puzzlermi.model;

/**
 * A position in a grid structure. It is defined through an x and a y coordinate, which values can only be non-negative integers.
 */
public interface Position extends Comparable<Position> {

    /**
     * Returns the x coordinate of this position.
     * @return the x coordinate of this position
     */
    int getX();

    /**
     * Returns the y coordinate of this position.
     * @return the y coordinate of this position
     */
    int getY();
}
