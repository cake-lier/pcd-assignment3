package it.unibo.pcd.assignment3.puzzlermi.model.impl;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.Tile;

import java.io.Serializable;

/**
 * A record implementation of the {@link Tile} interface.
 * @param originalPosition the {@link Position} of this {@link Tile} in the reordered puzzle
 * @param currentPosition the {@link Position} of this {@link Tile} in the current tiles' arrangement
 */
public record TileImpl(Position originalPosition, Position currentPosition) implements Tile, Serializable {

    @Override
    public Position getOriginalPosition() {
        return this.originalPosition;
    }

    @Override
    public Position getCurrentPosition() {
        return this.currentPosition;
    }

    @Override
    public boolean isInRightPlace() {
        return this.currentPosition.equals(this.originalPosition);
    }

    @Override
    public int compareTo(final Tile other) {
        return this.currentPosition.compareTo(other.getCurrentPosition());
    }
}
