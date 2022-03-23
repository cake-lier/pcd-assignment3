package it.unibo.pcd.assignment3.puzzlermi.model.impl;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;

import java.io.Serializable;

/**
 * A record implementation of the {@link Position} interface.
 * @param x the x coordinate of the created {@link Position}
 * @param y the y coordinate of the created {@link Position}
 */
public record PositionImpl(int x, int y) implements Position, Serializable {

    @Override
    public int compareTo(final Position other) {
        return this.x != other.getX() ? this.x - other.getX() : (this.y - other.getY());
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }
}
