package it.unibo.pcd.assignment3.view;

import it.unibo.pcd.assignment3.model.Tile;

public interface SelectionManager {

    void selectTile(Tile tile, Runnable onSwapPerformed);
}
