package it.unibo.pcd.assignment3.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.SortedMap;

public interface RemoteGatekeeper extends Remote {

    SortedMap<Peer, RemotePuzzle> getRemotePuzzles() throws RemoteException;

    SortedMap<Peer, RemoteSemaphore> getRemoteSemaphores() throws RemoteException;

    void registerPeer(Peer peer, RemotePuzzle puzzle, RemoteSemaphore semaphore) throws RemoteException;

    void unregisterPeer(Peer peer) throws RemoteException;

    void unregisterPeers(Collection<? extends Peer> peers) throws RemoteException;
}
