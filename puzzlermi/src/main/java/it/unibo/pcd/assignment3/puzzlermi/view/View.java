package it.unibo.pcd.assignment3.puzzlermi.view;

import it.unibo.pcd.assignment3.puzzlermi.model.Tile;

import java.util.List;

public interface View {

    void displayTiles(List<Tile> tiles);

    void displaySolution();
}
