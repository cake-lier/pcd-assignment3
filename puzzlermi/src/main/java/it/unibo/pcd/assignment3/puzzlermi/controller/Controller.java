package it.unibo.pcd.assignment3.puzzlermi.controller;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.Tile;

import java.util.List;

public interface Controller {

    void exit();

    List<Tile> getTiles();

    void swap(Position firstPosition, Position secondPosition);
}
