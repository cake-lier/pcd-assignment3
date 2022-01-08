package it.unibo.pcd.assignment3.puzzlermi.controller;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;

public interface Controller {

    void exit();

    void swap(Position firstPosition, Position secondPosition);
}
