package it.unibo.pcd.assignment3.puzzlermi.controller.impl;

import it.unibo.pcd.assignment3.puzzlermi.controller.LocalGatekeeper;
import it.unibo.pcd.assignment3.puzzlermi.controller.Peer;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemoteGatekeeper;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.puzzlermi.controller.RemoteSemaphore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The implementation of the gatekeeper entity, combining both its local and remote interfaces.
 */
public class GatekeeperImpl implements LocalGatekeeper, RemoteGatekeeper {
    private final SortedMap<Peer, RemotePuzzle> localPuzzles;
    private final SortedMap<Peer, RemotePuzzle> remotePuzzles;
    private final SortedMap<Peer, RemoteSemaphore> localSemaphores;
    private final SortedMap<Peer, RemoteSemaphore> remoteSemaphores;

    /**
     * Constructor for creating a gatekeeper associated with a {@link Peer} which is an "extra {@link Peer}", one which is not
     * the first in joining the game session which desires to join.
     * @param self the {@link Peer} using this gatekeeper as interface
     * @param puzzle the {@link RemotePuzzle} owned by the {@link Peer} using this gatekeeper as interface
     * @param puzzleStub the stub of the given {@link RemotePuzzle} for allowing its remote access
     * @param semaphore the {@link RemoteSemaphore} owned by the {@link Peer} using this gatekeeper as interface
     * @param semaphoreStub the stub of the given {@link RemoteSemaphore} for allowing its remote access
     * @param puzzles the {@link Map} associating every other {@link Peer} in the system with the {@link RemotePuzzle}s they own
     * @param semaphores the {@link Map} associating every other {@link Peer} in the system with the {@link RemoteSemaphore}s they
     *                   own
     * @throws RemoteException if the method fails while executing remotely
     */
    public GatekeeperImpl(
        final Peer self,
        final RemotePuzzle puzzle,
        final RemotePuzzle puzzleStub,
        final RemoteSemaphore semaphore,
        final RemoteSemaphore semaphoreStub,
        final SortedMap<Peer, RemotePuzzle> puzzles,
        final SortedMap<Peer, RemoteSemaphore> semaphores
    ) throws RemoteException {
        this.localPuzzles = new TreeMap<>(puzzles);
        this.localPuzzles.put(self, puzzle);
        this.remotePuzzles = new TreeMap<>(puzzles);
        this.remotePuzzles.put(self, puzzleStub);
        this.localSemaphores = new TreeMap<>(semaphores);
        this.localSemaphores.put(self, semaphore);
        this.remoteSemaphores = new TreeMap<>(semaphores);
        this.remoteSemaphores.put(self, semaphoreStub);
    }

    /**
     * Constructor for creating a gatekeeper associated with a {@link Peer} which is the first in joining a game session.
     * @param self the {@link Peer} using this gatekeeper as interface
     * @param puzzle the {@link RemotePuzzle} owned by the {@link Peer} using this gatekeeper as interface
     * @param semaphore the {@link RemoteSemaphore} owned by the {@link Peer} using this gatekeeper as interface
     * @throws RemoteException if the method fails while executing remotely
     */
    public GatekeeperImpl(final Peer self, final RemotePuzzle puzzle, final RemoteSemaphore semaphore)
        throws RemoteException {
        this.localPuzzles = new TreeMap<>(Map.of(self, puzzle));
        this.remotePuzzles = new TreeMap<>(Map.of(self, (RemotePuzzle) UnicastRemoteObject.exportObject(puzzle, 0)));
        this.localSemaphores = new TreeMap<>(Map.of(self, semaphore));
        this.remoteSemaphores = new TreeMap<>(Map.of(self, (RemoteSemaphore) UnicastRemoteObject.exportObject(semaphore, 0)));
    }

    @Override
    public SortedMap<Peer, RemotePuzzle> getLocalPuzzles() {
        return new TreeMap<>(this.localPuzzles);
    }

    @Override
    public SortedMap<Peer, RemoteSemaphore> getLocalSemaphores() {
        return new TreeMap<>(this.localSemaphores);
    }

    @Override
    public SortedMap<Peer, RemotePuzzle> getRemotePuzzles() {
        return new TreeMap<>(this.remotePuzzles);
    }

    @Override
    public SortedMap<Peer, RemoteSemaphore> getRemoteSemaphores() {
        return new TreeMap<>(this.remoteSemaphores);
    }

    @Override
    public void registerPeer(final Peer peer, final RemotePuzzle puzzle, final RemoteSemaphore semaphore) throws RemoteException {
        this.localPuzzles.put(peer, puzzle);
        this.remotePuzzles.put(peer, puzzle);
        this.localSemaphores.put(peer, semaphore);
        this.remoteSemaphores.put(peer, semaphore);
    }

    @Override
    public void unregisterPeer(final Peer peer) {
        this.localPuzzles.remove(peer);
        this.remotePuzzles.remove(peer);
        this.localSemaphores.remove(peer);
        this.remoteSemaphores.remove(peer);
    }

    @Override
    public void unregisterPeers(final Collection<? extends Peer> peers) throws RemoteException {
        peers.forEach(this::unregisterPeer);
    }

    @Override
    public String toString() {
        return "GatekeeperImpl{localPuzzles=" + this.localPuzzles + ", remotePuzzles=" + this.remotePuzzles + ", localSemaphores="
               + this.localSemaphores + ", remoteSemaphores=" + this.remoteSemaphores + "}";
    }
}
