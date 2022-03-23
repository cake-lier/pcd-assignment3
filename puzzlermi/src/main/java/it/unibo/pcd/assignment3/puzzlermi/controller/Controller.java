package it.unibo.pcd.assignment3.puzzlermi.controller;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;

/**
 * The Controller component of this application, it should represent the application itself. That being so, it receives user
 * input from the View component and notifies it of changes in the Model component state. It should also be capable of notifying
 * the Model of requests made by the user and receive the adequate response. At last, it should manage the application state.
 */
public interface Controller {

    /**
     * It exits the application.
     */
    void exit();

    /**
     * Swaps the two {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile}s corresponding to the two {@link Position}s given.
     * @param firstPosition the {@link Position} of the first {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile} to swap
     * @param secondPosition the {@link Position} of the second {@link it.unibo.pcd.assignment3.puzzlermi.model.Tile} to swap
     */
    void swap(Position firstPosition, Position secondPosition);
}
