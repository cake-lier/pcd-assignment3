package it.unibo.pcd.assignment3.controller;

import it.unibo.pcd.assignment3.model.Tile;

import java.util.List;

public interface Controller {

    void exit();

    List<Tile> getTiles();

    void swap(Tile firstTile, Tile secondTile);
}
