package it.unibo.pcd.assignment3.puzzlermi.model;

public interface Tile extends Comparable<Tile> {

    Position getOriginalPosition();

    Position getCurrentPosition();

    boolean isInRightPlace();
}
