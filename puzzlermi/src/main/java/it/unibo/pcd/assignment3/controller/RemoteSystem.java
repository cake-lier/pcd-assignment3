package it.unibo.pcd.assignment3.controller;

import it.unibo.pcd.assignment3.model.Tile;

import java.util.List;

public interface RemoteSystem {

    List<Tile> getTiles();

    void requestSwap(Tile firstTile, Tile secondTile);
}
