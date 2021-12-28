package it.unibo.pcd.assignment3.puzzlermi.model;

import java.util.List;

public interface PuzzleBoard {

    List<Tile> getTiles();

    void swap(Position firstPosition, Position secondPosition);

    boolean isSolution();
}
