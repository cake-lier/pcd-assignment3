package it.unibo.pcd.assignment3.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.SortedSet;

public interface AddressBook extends Remote {

    SortedSet<Peer> getPeers(Peer peer) throws RemoteException;

    void registerPeer(Peer peer) throws RemoteException;

    void unregisterPeer(Peer peer) throws RemoteException;
}
