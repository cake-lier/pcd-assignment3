package it.unibo.pcd.assignment3.puzzlermi.controller;

import it.unibo.pcd.assignment3.puzzlermi.model.Position;
import it.unibo.pcd.assignment3.puzzlermi.model.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * A remote object representing the puzzle that a given peer owns and the other peers can act upon for swapping its {@link Tile}s.
 */
public interface RemotePuzzle extends Remote {

    /**
     * Swaps the two {@link Tile}s in the puzzle corresponding to the two {@link Position}s given.
     * @param firstPosition the {@link Position} of the first {@link Tile} to swap
     * @param secondPosition the {@link Position} of the second {@link Tile} to swap
     * @throws RemoteException if the method fails while executing remotely
     */
    void swap(Position firstPosition, Position secondPosition) throws RemoteException;

    /**
     * Returns all the {@link Tile}s that make up the puzzle, ordered from the top left to the bottom right one following the
     * current arrangement.
     * @return all the {@link Tile}s that make up the puzzle
     */
    List<Tile> getTiles() throws RemoteException;
}
