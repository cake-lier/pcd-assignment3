package it.unibo.pcd.assignment3.puzzlermi.controller;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.SortedMap;

/**
 * The remote component of the gatekeeper entity, which is the interface that the local {@link Peer} shows to the other
 * {@link Peer}s in the system. This means that its instances are remote objects.
 */
public interface RemoteGatekeeper extends Remote {

    /**
     * Returns a remote copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     * corresponding {@link RemotePuzzle}s.
     * @return a remote copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     *         corresponding {@link RemotePuzzle}s
     * @throws RemoteException if the method fails while executing remotely
     */
    SortedMap<Peer, RemotePuzzle> getRemotePuzzles() throws RemoteException;

    /**
     * Returns a remote copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     * corresponding {@link RemoteSemaphore}s.
     * @return a remote copy of the {@link java.util.Map} that associates the {@link Peer} in the system with their
     *         corresponding {@link RemoteSemaphore}s
     * @throws RemoteException if the method fails while executing remotely
     */
    SortedMap<Peer, RemoteSemaphore> getRemoteSemaphores() throws RemoteException;

    /**
     * Registers a new {@link Peer} on this gatekeeper associating its identity with the given {@link RemotePuzzle} and the
     * given {@link RemoteSemaphore}
     * @param peer the {@link Peer} to register in this gatekeeper
     * @param puzzle the {@link RemotePuzzle} owned by the registered {@link Peer}
     * @param semaphore the {@link RemoteSemaphore} owned by the registered {@link Peer}
     * @throws RemoteException if the method fails while executing remotely
     */
    void registerPeer(Peer peer, RemotePuzzle puzzle, RemoteSemaphore semaphore) throws RemoteException;

    /**
     * Unregisters the given {@link Peer} from this gatekeeper.
     * @param peer the {@link Peer} to unregister
     * @throws RemoteException if the method fails while executing remotely
     */
    void unregisterPeer(Peer peer) throws RemoteException;

    /**
     * Unregisters all the {@link Peer}s contained into the given {@link Collection}.
     * @param peers the {@link Collection} containing the {@link Peer}s to unregister
     * @throws RemoteException if the method fails while executing remotely
     */
    void unregisterPeers(Collection<? extends Peer> peers) throws RemoteException;
}
