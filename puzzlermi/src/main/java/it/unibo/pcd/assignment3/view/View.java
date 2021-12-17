package it.unibo.pcd.assignment3.view;

import it.unibo.pcd.assignment3.model.Tile;

import java.util.List;

public interface View {

    void displayTiles(List<Tile> tiles);

    void displaySolution();
}
