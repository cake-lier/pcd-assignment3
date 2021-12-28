package it.unibo.pcd.assignment3.puzzlermi.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteSemaphore extends Remote {

    void acquire(Peer peer) throws RemoteException;

    void release(Peer peer) throws RemoteException;
}
