package it.unibo.pcd.assignment3.controller;

import it.unibo.pcd.assignment3.model.Position;
import it.unibo.pcd.assignment3.model.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemotePuzzle extends Remote {

    void swap(Position firstPosition, Position secondPosition) throws RemoteException;

    List<Tile> getTiles() throws RemoteException;
}
