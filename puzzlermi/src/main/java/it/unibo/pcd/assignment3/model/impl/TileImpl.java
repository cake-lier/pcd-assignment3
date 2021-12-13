package it.unibo.pcd.assignment3.model.impl;

import it.unibo.pcd.assignment3.model.Position;
import it.unibo.pcd.assignment3.model.Tile;

public record TileImpl(Position originalPosition, Position currentPosition) implements Tile {

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
