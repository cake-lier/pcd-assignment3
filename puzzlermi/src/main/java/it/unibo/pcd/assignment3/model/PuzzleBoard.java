package it.unibo.pcd.assignment3.model;

import java.util.List;

public interface PuzzleBoard {

    List<Tile> getTiles();

    void swap(Position firstPosition, Position secondPosition);

    boolean isSolution();
}
