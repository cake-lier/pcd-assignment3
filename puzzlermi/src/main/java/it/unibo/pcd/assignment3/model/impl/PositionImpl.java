package it.unibo.pcd.assignment3.model.impl;

import it.unibo.pcd.assignment3.model.Position;

import java.io.Serializable;

public record PositionImpl(int x, int y) implements Position, Serializable {
    @Override
    public int compareTo(final Position other) {
        return this.x != other.getX() ? this.x - other.getX() : (this.y != other.getY() ? this.y - other.getY() : 0);
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
