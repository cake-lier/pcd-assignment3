package it.unibo.pcd.assignment3.puzzlermi.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A remote object representing a semaphore on which the peers can synchronize for doing mutual exclusion operations.
 */
public interface RemoteSemaphore extends Remote {

    /**
     * Performs the "acquire" operation on this semaphore. The identity of the {@link Peer} must be given to ensure that no
     * other {@link Peer} can release it.
     * @param peer the {@link Peer} that is acquiring this semaphore
     * @throws RemoteException if the method fails while executing remotely
     */
    void acquire(Peer peer) throws RemoteException;

    /**
     * Performs the "release" operation on this semaphore. The identity of the {@link Peer} must be given to ensure that the
     * {@link Peer} releasing it is the one that previously acquired it.
     * @param peer the {@link Peer} that is releasing this semaphore
     * @throws RemoteException if the method fails while executing remotely
     */
    void release(Peer peer) throws RemoteException;
}
