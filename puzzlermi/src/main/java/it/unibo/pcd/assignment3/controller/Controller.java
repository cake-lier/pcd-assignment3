package it.unibo.pcd.assignment3.controller;

import it.unibo.pcd.assignment3.model.Tile;

public interface Controller {

    void exit();

    void swap(Tile firstTile, Tile secondTile);
}
