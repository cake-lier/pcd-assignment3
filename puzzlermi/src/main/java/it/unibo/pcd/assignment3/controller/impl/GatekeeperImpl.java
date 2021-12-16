package it.unibo.pcd.assignment3.controller.impl;

import it.unibo.pcd.assignment3.controller.LocalGatekeeper;
import it.unibo.pcd.assignment3.controller.Peer;
import it.unibo.pcd.assignment3.controller.RemoteGatekeeper;
import it.unibo.pcd.assignment3.controller.RemotePuzzle;
import it.unibo.pcd.assignment3.controller.RemoteSemaphore;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GatekeeperImpl implements LocalGatekeeper, RemoteGatekeeper {
    private final SortedMap<Peer, RemotePuzzle> localPuzzles;
    private final SortedMap<Peer, RemotePuzzle> remotePuzzles;
    private final SortedMap<Peer, RemoteSemaphore> localSemaphores;
    private final SortedMap<Peer, RemoteSemaphore> remoteSemaphores;

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
}
